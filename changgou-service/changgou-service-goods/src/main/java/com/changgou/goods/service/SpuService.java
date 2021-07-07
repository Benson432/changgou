package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;

import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:Spu业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SpuService {

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
     Spu findById(Long id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();

    /**
     * @description: 添加商品
     * @param goods 商品对象
     * @return:
     * @author: Benson
     * @time: 2021/7/6 0:09
     */
    void saveGoods(Goods goods);

    /**
     * @description: 根据id查找商品
     * @param id SPU ID
     * @return: com.changgou.goods.pojo.Goods
     * @author: Benson
     * @time: 2021/7/6 23:51
     */
    Goods findGoodsById(Long id);

    /**
     * @description: 商品审核
     * @param id spuId
     * @return: void
     * @author: Benson
     * @time: 2021/7/7 11:33
     */
    void audit(Long id);

    /**
     * @description: 商品下架
     * @param id spuId
     * @return: void
     * @author: Benson
     * @time: 2021/7/7 11:33
     */
    void pull(Long id);

    /**
     * @description: 商品上架
     * @param id spuId
     * @return: void
     * @author: Benson
     * @time: 2021/7/7 11:33
     */
    void put(Long id);


    /**
     * @description: 批量上架
     * @param ids id数组
     * @return: void
     * @author: Benson
     * @time: 2021/7/7 22:47
     */
    void putMany(Long[] ids);
}
