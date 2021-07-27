package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @description:
 * @author: Benson
 * @time: 2021/7/19 11:40
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Resource
    private SkuFeign skuFeign;

    @Resource
    private SkuEsMapper skuEsMapper;

    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public void importSku() {
        //调用changgou-service-goods微服务
        Result<List<Sku>> skuListResult = skuFeign.findByStatus("1");
        //将数据转成search.Sku
        List<SkuInfo> skuInfos=  JSON.parseArray(JSON.toJSONString(skuListResult.getData()),SkuInfo.class);
        for(SkuInfo skuInfo:skuInfos){
            Map<String, Object> specMap= JSON.parseObject(skuInfo.getSpec()) ;
            skuInfo.setSpecMap(specMap);
        }
        skuEsMapper.saveAll(skuInfos);
    }

    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //组合条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (searchMap != null) {
            if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")));
            }
            if (!StringUtils.isEmpty(searchMap.get("category"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
            }
            if (!StringUtils.isEmpty(searchMap.get("brand"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            if (!StringUtils.isEmpty(searchMap.get("price"))) {
                //价格区间
                String price = searchMap.get("price").replace("元", "").replace("以上", "");
                String[] prices = price.split("-");
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                if (prices.length > 1) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                }
            }
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                //规格
                if (entry.getKey().startsWith("spec_") && !StringUtils.isEmpty(entry.getValue())) {
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap."+entry.getKey().substring(5)+".keyword", entry.getValue()));
                }
            }
            //排序
            if (!StringUtils.isEmpty(searchMap.get("sortField")) && !StringUtils.isEmpty(searchMap.get("sortRule"))) {
                SortOrder sortOrder;
                if (searchMap.get("sortRule").equalsIgnoreCase("ASC")) {
                    sortOrder = SortOrder.ASC;
                } else {
                    sortOrder = SortOrder.DESC;
                }
                nativeSearchQueryBuilder.withSort(new FieldSortBuilder(searchMap.get("sortField")).order(sortOrder));
            }
        }
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        //高亮
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        field.preTags("<em style=\"color:red;\">");
        field.postTags("</em>");
        field.fragmentSize(100);
        nativeSearchQueryBuilder.withHighlightFields(field);

        //分页
        int pageSize = 30;
        int currentPage = getPageNum(searchMap);
        nativeSearchQueryBuilder.withPageable(PageRequest.of(currentPage, pageSize));


        //执行搜索
        AggregatedPage<SkuInfo> skuInfoAggregatedPage = elasticsearchTemplate.queryForPage(
                nativeSearchQueryBuilder.build(),
                SkuInfo.class,
                new SearchResultMapper() {
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                        //高亮处理
                        List<T> list = new ArrayList<T>();
                        for (SearchHit hit : searchResponse.getHits()) {
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            HighlightField highlightField =  hit.getHighlightFields().get("name");
                            if (highlightField != null && highlightField.getFragments() != null) {
                                Text[] fragments = highlightField.getFragments();
                                StringBuffer stringBuffer = new StringBuffer();
                                for (Text fragment : fragments) {
                                    stringBuffer.append(fragment.toString());
                                }
                                skuInfo.setName(stringBuffer.toString());
                            }
                            list.add((T)skuInfo);
                        }
                        return new AggregatedPageImpl<T>(list, pageable, searchResponse.getHits().getTotalHits());
                    }
                }
        );


        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, searchMap);

        resultMap.putAll(groupMap);
        resultMap.put("rows", skuInfoAggregatedPage.getContent());
        resultMap.put("total", skuInfoAggregatedPage.getTotalElements());
        resultMap.put("totalPages", skuInfoAggregatedPage.getTotalPages());
        resultMap.put("currentPage", currentPage);
        resultMap.put("pageSize", pageSize);

        return resultMap;
    }


    /**
     * @description: 获取pageNum
     * @param searchMap
     * @return: java.lang.Integer
     * @author: Benson
     * @time: 2021/7/22 0:02
     */
    public Integer getPageNum(Map<String, String> searchMap) {
        if (searchMap != null) {
            try {
                int pageNum = Integer.parseInt(searchMap.get("pageNum"));
                return Math.max(pageNum, 1);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }


    /**
     * @description: 查询 分类、品牌、规格数据
     * @param nativeSearchQueryBuilder
     * @param searchMap 搜索条件
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     * @author: Benson
     * @time: 2021/7/23 12:00
     */
    public Map<String, Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder, Map<String ,String> searchMap) {
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            //查询分类
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            //查询品牌
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }
        //查询规格
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        //执行查询
        AggregatedPage<SkuInfo> skuInfoAggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);


        Map<String, Object> resultMap = new HashMap<>();
        //获取分组数据
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms stringTerms = skuInfoAggregatedPage.getAggregations().get("skuCategory");
            List<String> categoryList = getGroupList(stringTerms);
            resultMap.put("categoryList", categoryList);
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms stringTerms = skuInfoAggregatedPage.getAggregations().get("skuBrand");
            List<String> brandList = getGroupList(stringTerms);
            resultMap.put("brandList", brandList);
        }
        StringTerms stringTerms = skuInfoAggregatedPage.getAggregations().get("skuSpec");
        resultMap.put("specList", getAllSpec(stringTerms, searchMap));
        return resultMap;
    }


    /**
     * @description: 将StringTerms处理为List
     * @param stringTerms
     * @return: java.util.List<java.lang.String>
     * @author: Benson
     * @time: 2021/7/23 12:01
     */
    public List<String> getGroupList(StringTerms stringTerms) {
        List<String> list = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String categoryName = bucket.getKeyAsString();
            list.add(categoryName);
        }
        return list;
    }


    /**
     * @description: 规格处理
     * @param stringTerms
     * @param searchMap
     * @return: java.util.Map<java.lang.String,java.util.Set<java.lang.String>>
     * @author: Benson
     * @time: 2021/7/23 12:02
     */
    public Map<String, Set<String>> getAllSpec(StringTerms stringTerms, Map<String, String> searchMap) {
        //合并之后的Map
        Map<String, Set<String>> allSpec = new HashMap<String, Set<String>>();
        //处理合并
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String spec = bucket.getKeyAsString();
            Map<String, String> specMap = JSON.parseObject(spec, Map.class);
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                Set<String> specSet;
                if (allSpec.containsKey(entry.getKey())) {
                    specSet = allSpec.get(entry.getKey());
                } else {
                    specSet = new HashSet<String>();
                }
                specSet.add(entry.getValue());
                allSpec.put(entry.getKey(), specSet);
            }
        }

        //过滤已查询规格
        for (Map.Entry<String, String> entry : searchMap.entrySet()) {
            if (entry.getKey().startsWith("spec_") && !StringUtils.isEmpty(entry.getValue())) {
                allSpec.remove(entry.getKey().substring(5));
            }
        }
        return allSpec;
    }








}
