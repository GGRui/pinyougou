package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Goods;

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

    /**
     * 新增
     * @param goods 实体
     * @return 操作结果
     */
    void addGoods(Goods goods);

    /**
     * 根据主键查询
     * @param id 主键
     * @return 实体
     */
    Goods findGoodsById(Long id);

    /**
     * 修改商品
     * @param goods 实体
     * @return 操作结果
     */
    void updateGoods(Goods goods);

    /**
     * 根据商品spu id数组更新商品spu 的审核状态
     * @param status 商品状态spu
     * @param ids 商品spu id数组
     * @return 操作结果
     */
    void updateStatus(Long[] ids, String status);

    /**
     * 根据主键数组批量删除
     * @param ids 主键数组
     * @return 实体
     */
    void deleteByGoodsIds(Long[] ids);
}
