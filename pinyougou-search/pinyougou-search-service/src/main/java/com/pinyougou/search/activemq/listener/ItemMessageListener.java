package com.pinyougou.search.activemq.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;

public class ItemMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        //- 接收消息转换为一个商品sku列表
        TextMessage textMessage = (TextMessage) message;
        List<TbItem> itemList = JSON.parseArray(textMessage.getText(), TbItem.class);
        //- 调用业务方法更新es数据
        itemSearchService.importItemList(itemList);
        System.out.println("同步了" + itemList.size() + " 个商品到es中...");
    }
}
