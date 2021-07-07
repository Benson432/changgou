package com.changgou.goods.dao;
import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


/****
 * @Author:shenkunlin
 * @Description:Brand的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface BrandMapper extends Mapper<Brand> {

    /**
     * @description: 根据分类id查询品牌集合
     * @param categoryId 分类id
     * @return: java.util.List<com.changgou.goods.pojo.Brand>
     * @author: Benson
     * @time: 2021/7/5 22:09
     */
    @Select("SELECT tb.* FROM tb_brand as tb JOIN tb_category_brand tcb ON tb.id = tcb.brand_id WHERE tcb.category_id = #{categoryId}")
    List<Brand> findByCategory(Integer categoryId);
}
