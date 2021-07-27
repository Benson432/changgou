package com.changgou.search.controller;

import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @description:
 * @author: Benson
 * @time: 2021/7/19 11:53
 */
@RestController
@RequestMapping("/search")
@CrossOrigin
public class SkuController {

    @Resource
    private SkuService skuService;

    @GetMapping("/import")
    public Result search(){
        skuService.importSku();
        return new Result();
    }

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String, String> searchMap){
        return  skuService.search(searchMap);
    }
}
