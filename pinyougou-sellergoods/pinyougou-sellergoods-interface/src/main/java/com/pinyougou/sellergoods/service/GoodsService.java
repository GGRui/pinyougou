package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface GoodsService extends BaseService<TbGoods> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param goods 搜索条件
     * @return 分页信息
     */
    PageInfo<TbGoods> search(Integer pageNum, Integer pageSize, TbGoods goods);

}
