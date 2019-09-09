package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl extends BaseServiceImpl<TbTypeTemplate> implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageInfo<TbTypeTemplate> search(Integer pageNum, Integer pageSize, TbTypeTemplate typeTemplate) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbTypeTemplate.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        if (StringUtils.isNotBlank(typeTemplate.getName())) {
            criteria.andLike("name", "%" + typeTemplate.getName() + "%");
        }

        List<TbTypeTemplate> list = typeTemplateMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public List<Map> findSpecList(Long id) {
        //1.根据分类模板id查询分类模板
        TbTypeTemplate typeTemplate = findOne(id);
        List<Map> specMapList = null;

        if (StringUtils.isNotBlank(typeTemplate.getSpecIds())){
            /**
             * 参数1：要转换的json字符串
             * 参数2：转换之后的对象类型
             */
            specMapList = JSON.parseArray(typeTemplate.getSpecIds(),Map.class);

            //2.遍历每个规格，根据规格查询气选项
            for (Map map : specMapList) {
                //规格id
                String specId = map.get("id").toString();
                //根据规格id查询规格选项
                TbSpecificationOption param = new TbSpecificationOption();
                param.setSpecId(Long.parseLong(specId));

                List<TbSpecificationOption> options = specificationOptionMapper.select(param);
                map.put("options", options);
            }
        }
        //3.返回
        return specMapList;
    }

}
