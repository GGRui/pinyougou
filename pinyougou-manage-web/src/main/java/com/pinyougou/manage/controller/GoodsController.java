package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.Result;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.jms.*;
import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference(retries = 0, timeout = 10000)
    private GoodsService goodsService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQQueue itemEsQueue;

    @Autowired
    private ActiveMQQueue itemEsDeleteQueue;

    @Autowired
    private ActiveMQTopic itemTopic;

    @Autowired
    private ActiveMQTopic itemDeleteTopic;



    /**
     * 新增
     * @param goods 实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods){
        try {
            //设置商家
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.getGoods().setSellerId(sellerId);
            //未审核
            goods.getGoods().setAuditStatus("0");
            goodsService.addGoods(goods);

            return Result.ok("新增成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增失败");
    }

    /**
     * 根据主键查询
     * @param id 主键
     * @return 实体
     */
    @GetMapping("/findOne/{id}")
    public Goods findOne(@PathVariable Long id){
        return goodsService.findGoodsById(id);
    }

    /**
     * 修改商品
     * @param goods 实体
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        try {
            TbGoods oldGoods = goodsService.findOne(goods.getGoods().getId());
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (sellerId.equals(oldGoods.getSellerId())){
                goodsService.updateGoods(goods);
            }else {
                //判断商家修改的商品是否为自己的产品
                return Result.fail("非法修改商品!");
            }
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    /**
     * 根据商品spu id数组更新商品spu 的审核状态
     * @param status 商品状态spu
     * @param ids 商品spu id数组
     * @return 操作结果
     */
    @GetMapping("/updateStatus")
    public Result updateStatus(String status,Long[] ids){
        try {
            goodsService.updateStatus(ids,status);
            //同步搜索系统数据
            if ("2".equals(status)){
                //审核通过：根据sku id数组和已启用状态（1）查询商品sku列表
                List<TbItem> itemList = goodsService.findItemListByGoodsIdsAndItemStatus(ids,"1");

                //更新系统中的数据
                //itemSearchService.importItemList(itemList);

                jmsTemplate.send(itemEsQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage();
                        textMessage.setText(JSON.toJSONString(itemList));
                        return textMessage;
                    }
                });
                //发送商品被审核通过的主题消息
                sendMQMessage(itemTopic,ids);
            }
            return Result.ok("更新商品状态成功!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("更新商品状态成功!");
    }

    /**
     * 根据主键数组批量删除
     * @param ids 主键数组
     * @return 实体
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            //goodsService.deleteByIds(ids);
            goodsService.deleteByGoodsIds(ids);

            //同步删除搜索系统中数据
            sendMQMessage(itemEsDeleteQueue,ids);
            //发送商品被删除的主题消息
            sendMQMessage(itemDeleteTopic,ids);
            //itemSearchService.deleteByGoodsIds(ids);

            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    private void sendMQMessage(Destination destination, Long[] ids) {
        jmsTemplate.send(destination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage objectMessage = session.createObjectMessage();
                objectMessage.setObject(ids);
                return objectMessage;
            }
        });
    }

    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param goods 搜索条件
     * @return 分页信息
     */
    @PostMapping("/search")
    public PageInfo<TbGoods> search(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                    @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                    @RequestBody TbGoods goods) {
        return goodsService.search(pageNum, pageSize, goods);
    }

}