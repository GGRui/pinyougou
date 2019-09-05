package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.service.BaseService;

import java.util.List;
import java.util.Map;

public interface BrandService extends BaseService<TbBrand> {
    /**
     * 查询所有品牌数据
     * @return 品牌列表
     */
    List<TbBrand> queryAll();

    /**
     * 分页查询所有分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<TbBrand> testPage(Integer pageNum, Integer pageSize);


    /**
     * 根据分页信息和查询条件分页模糊查询品牌数据
     * @param pageNum 页号
     * @param pageSize 页大小
     * @param brand 查询条件
     * @return 分页信息对象
     */
    PageInfo<TbBrand> search(Integer pageNum, Integer pageSize, TbBrand brand);

    /**
     * 查询所有品牌列表；格式为：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
     * @return 品牌列表；格式为：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
     */
    List<Map<String,String>> selectOptionList();
}
