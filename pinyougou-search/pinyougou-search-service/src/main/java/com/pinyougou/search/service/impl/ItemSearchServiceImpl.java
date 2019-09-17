package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.search.dao.ItemDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private ItemDao itemDao;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();

        //搜索对象构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //如果没有搜索条件则查询全部
        queryBuilder.withQuery(QueryBuilders.matchAllQuery());

        //是否要高亮
        boolean isHighLight = false;

        if(searchMap != null){
            String keywords = searchMap.get("keywords")+"";
            if (StringUtils.isNotBlank(keywords)){
                queryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords,"title","brand","category","seller").operator(Operator.AND));

                //高亮配置
                isHighLight = true;


                //设置高亮区域
                HighlightBuilder.Field highLightField = new HighlightBuilder.Field("title");
                //高亮的起始标签
                highLightField.preTags("<span style='color:red'>");
                //高亮结束标签
                highLightField.postTags("</span>");

                queryBuilder.withHighlightFields(highLightField);
            }

            //创建组合条件用户条件过滤
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            //商品分类
            String category = searchMap.get("category")+"";
            if (StringUtils.isNotBlank(category)){
                //词条查询，不分词
                boolQueryBuilder.must(QueryBuilders.termQuery("category",category));
            }

            //品牌
            String brand = searchMap.get("brand")+"";
            if (StringUtils.isNotBlank(brand)){
                //词条查询；不分词
                boolQueryBuilder.must(QueryBuilders.termQuery("brand",brand));
            }

            //规格，使用嵌套查询
            Map<String,String> specMap = (Map<String,String>) searchMap.get("spec");
            if (specMap != null){
                for (Map.Entry<String, String> entry : specMap.entrySet()) {
                    //使用嵌套名中间部分
                    String key = "specMap." + entry.getKey() + ".keyword";
                    String value = entry.getValue();
                    NestedQueryBuilder query1 = QueryBuilders.nestedQuery("specMap",QueryBuilders.matchQuery(key, value), ScoreMode.Max);
                    boolQueryBuilder.must(query1);
                }
            }

            //价格
            String price = searchMap.get("price")+"";
            if (StringUtils.isNotBlank(price)) {
                //范围查询
                String[] prices = price.split("-");
                //查询大于价格下限的商品 price > ?
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(prices[0]));

                if (!"*".equals(prices[1])) {
                    //查询小于等于价格上限的商品 price <= ?
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(prices[1]));
                }
            }
            queryBuilder.withFilter(boolQueryBuilder);

            //设置分页信息
            int pageNo = 1;
            String pageNoStr = searchMap.get("pageNo")+"";
            if (StringUtils.isNotBlank(pageNoStr)) {
                pageNo = Integer.parseInt(pageNoStr);
            }
            int pageSize = 20;
            String pageSizeStr = searchMap.get("pageSize")+"";
            if (StringUtils.isNotBlank(pageSizeStr)) {
                pageSize = Integer.parseInt(pageSizeStr);
            }

            queryBuilder.withPageable(PageRequest.of(pageNo - 1, pageSize));

            //设置排序
            String sortField = searchMap.get("sortField")+"";
            String sortOrder = searchMap.get("sortOrder")+"";
            if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortOrder)){
                queryBuilder.withSort(SortBuilders.fieldSort(sortField).order("ASC".equals(sortOrder)? SortOrder.ASC:SortOrder.DESC));
            }
        }
        //搜索对象
        NativeSearchQuery query = queryBuilder.build();

        //搜索
        AggregatedPage<TbItem> pageResult;
        if (isHighLight){
            pageResult = esTemplate.queryForPage(query, TbItem.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<T> itemList = new ArrayList<>();

                    //处理高亮结果
                    //获取高亮数据
                    SearchHits hits = searchResponse.getHits();
                    TbItem item = null;
                    for (SearchHit hit : hits) {
                        //商品的json格式字符串
                        String itemJsonStr = hit.getSourceAsString();
                        item = JSON.parseObject(itemJsonStr, TbItem.class);

                        //获取高亮标题
                        HighlightField highlightField = hit.getHighlightFields().get("title");
                        if (highlightField != null) {
                            StringBuilder sb = new StringBuilder();
                            for (Text fragment : highlightField.getFragments()) {
                                sb.append(fragment.string());
                            }
                            //设置高亮标题
                            item.setTitle(sb.toString());
                        }
                        itemList.add((T)item);
                    }
                    //处理查询结果
                    return new AggregatedPageImpl<>(itemList, pageable, searchResponse.getHits().getTotalHits());
                }
            });
        }else {
            pageResult = esTemplate.queryForPage(query, TbItem.class);
        }

        //商品列表
        resultMap.put("itemList", pageResult.getContent());
        //总记录数
        resultMap.put("total", pageResult.getTotalElements());
        //总页数
        resultMap.put("totalPages", pageResult.getTotalPages());
        return resultMap;
    }

    @Override
    public void importItemList(List<TbItem> itemList) {
        for (TbItem tbItem : itemList) {
            Map specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(specMap);
        }
        itemDao.saveAll(itemList);
    }

    @Override
    public void deleteByGoodsIds(Long[] goodsIds) {
        itemDao.deleteByGoodsIdIn(goodsIds);
    }

}
