package com.changgou.search.service;

import java.util.Map;

public interface SkuService {
    /***
     * 导入SKU数据
     */
    void importSku();


    Map<String, Object> search(Map<String, String> searchMap);
}
