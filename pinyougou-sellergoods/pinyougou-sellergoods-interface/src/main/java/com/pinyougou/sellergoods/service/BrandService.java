package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import com.pinyougou.service.BaseService;

import java.util.List;

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


}
