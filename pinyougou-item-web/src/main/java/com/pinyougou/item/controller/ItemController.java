package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ItemController {

    @Reference
    private GoodsService goodsService;
    @Reference
    private ItemCatService itemCatService;

    /**
     * 在根据商品spu id查询商品vo的时候；里面的商品sku列表需要根据默认值进行降序排序。
     * @param goodsId 商品spu id
     * @return 商品详情的视图名称和数据
     */
    @GetMapping("/{goodsId}")
    public ModelAndView toItemPage(@PathVariable Long goodsId){
        ModelAndView mv = new ModelAndView("item");

        Goods goods = goodsService.findGoodsByIdAndStatus(goodsId,"1");

        //goods  商品spu基本信息
        mv.addObject("goods",goods.getGoods());
        //goodDesc  商品spu描述信息
        mv.addObject("goodsDesc",goods.getGoodsDesc());
        //itemList   商品sku列表
        mv.addObject("itemList",goods.getItemList());
        //itemCat1 1级商品分类中文名称
        TbItemCat itemCat1 = itemCatService.findOne(goods.getGoods().getCategory1Id());
        mv.addObject("itemCat1",itemCat1.getName());
        //itemCat2 2级商品分类中文名称
        TbItemCat itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id());
        mv.addObject("itemCat2",itemCat2.getName());
        //itemCat3 3级商品分类中文名称
        TbItemCat itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id());
        mv.addObject("itemCat3",itemCat3.getName());

        return mv;
    }
}
