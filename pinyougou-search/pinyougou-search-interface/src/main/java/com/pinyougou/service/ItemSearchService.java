package com.pinyougou.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 根据搜索条件搜索es中的商品数据
     * @param searchMap
     * @return
     */
    Map<String,Object> search(Map<String, Object> searchMap);

    /**
     * 根据sku id数组和已启用状态（1）查询商品sku列表
     * @param itemList 商品sku列表
     * @return 操作结果
     */
    void importItemList(List<TbItem> itemList);

    /**
     * 根据商品spu id数组删除在es中的sku
     * @param goodsIds spu id数组
     */
    void deleteByGoodsIds(Long[] goodsIds);
}
