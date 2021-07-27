package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: Benson
 * @time: 2021/7/23 17:08
 */

@Controller
@RequestMapping("/search")
public class SkuController {

    @Resource
    SkuFeign skuFeign;

    @GetMapping("/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) {
        Map resultMap = skuFeign.search(searchMap);
        model.addAttribute("result", resultMap);

        //分页
        Page<SkuInfo> pageInfo = new Page<>(
                Long.parseLong(resultMap.get("total").toString()),
                Integer.parseInt(resultMap.get("currentPage").toString()),
                Integer.parseInt(resultMap.get("pageSize").toString())
        );
        model.addAttribute("pageInfo",pageInfo);

        //搜索条件
        model.addAttribute("searchMap",searchMap);
        String[] urls = url(searchMap);
        model.addAttribute("url", urls[0]);
        model.addAttribute("unSortUrl", urls[1]);

        return "search";
    }

    public String[] url(Map<String, String> searchMap) {
        String url = "/search/list";
        String unSortUrl = "/search/list";
        if (searchMap != null && searchMap.size() != 0) {
            url += "?";
            unSortUrl += "?";
            for (Map.Entry<String, String> entry: searchMap.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("pageNum")) {
                    continue;
                }
                url += entry.getKey() + "=" + entry.getValue() + "&";
                if (entry.getKey().equalsIgnoreCase("sortField") || entry.getKey().equalsIgnoreCase("sortRule")) {
                    continue;
                }
                unSortUrl += entry.getKey() + "=" + entry.getValue() + "&";
            }
            url = url.substring(0, url.length() - 1);
            unSortUrl = url.substring(0, unSortUrl.length() - 1);
        }
        return new String[]{url, unSortUrl};
    }

}
