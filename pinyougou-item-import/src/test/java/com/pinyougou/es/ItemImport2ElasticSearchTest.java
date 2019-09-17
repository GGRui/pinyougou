package com.pinyougou.es;

import com.alibaba.fastjson.JSON;
import com.pinyougou.es.dao.ItemElasticSearchDao;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-*.xml")
public class ItemImport2ElasticSearchTest {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemElasticSearchDao itemElasticSearchDao;

    @Test
    public void itemImport(){
        TbItem tbItem = new TbItem();
        tbItem.setStatus("1");
        List<TbItem> itemList = itemMapper.select(tbItem);
        for (TbItem item : itemList) {
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            tbItem.setSpecMap(map);
        }

        itemElasticSearchDao.saveAll(itemList);
    }

}
