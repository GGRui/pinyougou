package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service(timeout = 10000)
public class ContentServiceImpl extends BaseServiceImpl<TbContent> implements ContentService {
    //在redis中对应的key
    private static final String REDIS_CONTENT_LIST = "CONTENT_LIST";

    @Autowired
    private ContentMapper contentMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void add(TbContent tbContent){
        super.add(tbContent);

        updateContentListInRedisByCategoryId(tbContent.getCategoryId());
    }

    /**
     * 跟新redis中某个内容分类对应缓存数据（实际都是删除redis的缓存）
     * @param categoryId
     */
    private void updateContentListInRedisByCategoryId(Long categoryId){
        redisTemplate.boundHashOps(REDIS_CONTENT_LIST).delete(categoryId);
    }

    @Override
    public void update(TbContent tbContent){
        //更新旧内容对应的内容分类缓冲
        TbContent oldContent = findOne(tbContent.getId());
        if (!oldContent.getCategoryId().equals(tbContent.getCategoryId())){
            //不相等说明内容被修改，更新旧内容对应的内容分类缓冲
            updateContentListInRedisByCategoryId(oldContent.getCategoryId());
        }
        super.update(tbContent);
        //更新新内容对应的内容分类缓冲
        updateContentListInRedisByCategoryId(tbContent.getCategoryId());
    }

    @Override
    public void deleteByIds(Serializable[] ids){
        // 根据当前选择的那些内容id查询内容列表
        Example example = new Example(TbContent.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        // 遍历内容列表根据内容分类id删除redis中的缓存数据
        List<TbContent> contentList = contentMapper.selectByExample(example);
        if (contentList != null && contentList.size() > 0){
            for (TbContent tbContent : contentList) {
                updateContentListInRedisByCategoryId(tbContent.getCategoryId());
            }
        }
        super.deleteByIds(ids);
    }

    @Override
    public PageInfo<TbContent> search(Integer pageNum, Integer pageSize, TbContent content) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbContent.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(content.getProperty())) {
            criteria.andLike("property", "%" + content.getProperty() + "%");
        }*/

        List<TbContent> list = contentMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {
        List<TbContent> contentList = null;
        try {
            contentList = (List<TbContent>) redisTemplate.boundHashOps(REDIS_CONTENT_LIST).get(categoryId);
            if (contentList != null){
                //如果存在直接返回
                return contentList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * --查询：根据内容分类id、状态查询内容数据并且根据排序属性降序排序
         * select * from tb_content where category_id=1 and status='1' order by sort_order desc
         */
        Example example = new Example(TbContent.class);
        example.createCriteria()
                .andEqualTo("status", "1")
                .andEqualTo("categoryId", categoryId);
        //根据排序属性降序排序
        example.orderBy("sortOrder").desc();

        contentList = contentMapper.selectByExample(example);

        try {
            redisTemplate.boundHashOps(REDIS_CONTENT_LIST).put(categoryId,contentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentList;
    }

}
