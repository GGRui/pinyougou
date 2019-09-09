package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GoodsServiceImpl extends BaseServiceImpl<TbGoods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsDescMapper goodsDescMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ItemCatMapper itemCatMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SellerMapper sellerMapper;

    @Override
    public PageInfo<TbGoods> search(Integer pageNum, Integer pageSize, TbGoods goods) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbGoods.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(goods.getProperty())) {
            criteria.andLike("property", "%" + goods.getProperty() + "%");
        }*/

        List<TbGoods> list = goodsMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void addGoods(Goods goods) {
        //1、保存商品基本信息
        add(goods.getGoods());
        //2、保存商品描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insertSelective(goods.getGoodsDesc());
        //3、保存商品sku 列表
        saveItemList(goods);
    }

    /**
     * 保存商品sku
     * @param goods 商品vo
     */
    private void saveItemList(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //启用规格
            if (goods.getItemList() != null && goods.getItemList().size() > 0) {
                for (TbItem tbItem : goods.getItemList()) {
                    String title = goods.getGoods().getGoodsName();
                    //sku标题 ：spu的商品名称 + 所有规格的选项值（spec中的规格对应的值)
                    if (StringUtils.isNotBlank(tbItem.getSpec())) {
                        Map<String, String> specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
                        for (Map.Entry<String, String> entry : specMap.entrySet()) {
                            title += " " + entry.getValue();
                        }
                    }
                    tbItem.setTitle(title);

                    setItemValue(tbItem, goods);

                    //保存商品sku
                    itemMapper.insertSelective(tbItem);
                }
            }
        } else {
            //不启用规格；创建一个sku
            TbItem tbItem = new TbItem();
            tbItem.setTitle(goods.getGoods().getGoodsName());
            //spec默认为{}
            tbItem.setSpec("{}");
            //price来自spu
            tbItem.setPrice(goods.getGoods().getPrice());
            tbItem.setNum(9999);
            //status: 0 未审核
            tbItem.setStatus("0");
            //isDefault：1默认
            tbItem.setIsDefault("1");

            setItemValue(tbItem, goods);

            itemMapper.insertSelective(tbItem);
        }
    }

    /**
     * 设置sku数据
     * @param tbItem sku商品
     * @param goods 商品vo
     */
    private void setItemValue(TbItem tbItem, Goods goods) {
        //图片 ：获取spu图片列表中的第一张图片
        if (StringUtils.isNotBlank(goods.getGoodsDesc().getItemImages())) {
            List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
            //在商品描述字段存储的数据结构：
            /**
             * [{"color":"黑色","url":"http://img11.360buyimg.com/n1/s450x450_jfs/t3076/42/8593902551/206108/fdb1a60f/58c60fc3Nf9faa2fa.jpg"},
             * {"color":"金色","url":"http://img11.360buyimg.com/n1/s450x450_jfs/t3076/42/8593902551/206108/fdb1a60f/58c60fc3Nf9faa2fa.jpg"}]
             */
            tbItem.setImage(imageList.get(0).get("url").toString());
        }
        //商品分类id：来自spu的第3级商品分类id
        tbItem.setCategoryid(goods.getGoods().getCategory3Id());
        //商品分类中文名称：根据spu的第3级商品分类id查找商品分类中文名称
        TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(tbItem.getCategoryid());
        tbItem.setCategory(tbItemCat.getName());

        //品牌中文名称：来spu的品牌id查询品牌
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        tbItem.setBrand(tbBrand.getName());
        //商家id：来自spu的商家id
        tbItem.setSellerId(goods.getGoods().getSellerId());
        //商家中文名称： 根据spu的商家id查询商家中文名称
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey(tbItem.getSellerId());
        tbItem.setSeller(tbSeller.getName());

        //其它属性从前台携带过来，要么复制自spu。
        tbItem.setGoodsId(goods.getGoods().getId());
        tbItem.setCreateTime(new Date());
        tbItem.setUpdateTime(tbItem.getCreateTime());
    }
}
