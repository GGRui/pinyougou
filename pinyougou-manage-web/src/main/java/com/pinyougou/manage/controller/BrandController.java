package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/brand")
@RestController
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     *
     * @return
     */
    @GetMapping("/findAll")
    public List<TbBrand> findAll(){
        /*return brandService.queryAll();*/
        return brandService.findAll();

    }

    /**
     * 分页查询品牌数据
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/testPage")
    public List<TbBrand> testPage
    (
         @RequestParam(defaultValue = "1") Integer pageNum,
         @RequestParam(defaultValue = "10") Integer pageSize
    ){
        //return brandService.testPage(pageNum,pageSize);
        return brandService.findPage(pageNum,pageSize).getList();
    }

    /**
     * 分页查询品牌数据
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/findPage")
    public PageInfo<TbBrand> findPage
    (
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ){
        //return brandService.testPage(pageNum,pageSize);
        return brandService.findPage(pageNum,pageSize);
    }
}
