package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/brand")
@RestController
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     *  查询所有品牌
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

    /**
     * 新增品牌
     * @param brand
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return Result.ok("新增品牌成功!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增品牌失败!");
    }

    /**
     * 根据id查询
     * @param id 品牌id
     * @return 品牌
     */
    @GetMapping("/findOne/{id}")
    public TbBrand findOne(@PathVariable Long id){
        return brandService.findOne(id);
    }

    /**
     * 批量删除品牌
     * @param ids 选中的品牌id
     * @return 返回品牌
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.deleteByIds(ids);
            return Result.ok("品牌删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("品牌删除失败!");
    }

    /**
     * 根据分页信息和查询条件分页模糊查询品牌数据
     * @param pageNum 页号
     * @param pageSize 页大小
     * @param brand 查询条件
     * @return 分页信息对象
     */
    @PostMapping("/search")
    public PageInfo<TbBrand> search
            (
               @RequestParam(defaultValue = "1") Integer pageNum,
               @RequestParam(defaultValue = "10") Integer pageSize,
               @RequestBody TbBrand brand
            ){
        return brandService.search(pageNum,pageSize,brand);
    }
}
