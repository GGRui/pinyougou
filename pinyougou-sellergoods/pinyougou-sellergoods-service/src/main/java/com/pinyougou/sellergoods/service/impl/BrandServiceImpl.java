package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class BrandServiceImpl extends BaseServiceImpl<TbBrand> implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public List<TbBrand> queryAll() {
        return brandMapper.queryAll();
    }

    @Override
    public List<TbBrand> testPage(Integer pageNum, Integer pageSize) {
        //设置分页
        PageHelper.startPage(pageNum,pageSize);

        //两种写法
        //return brandMapper.select(null);
        return brandMapper.selectAll();
    }

    @Override
    public PageInfo<TbBrand> search(Integer pageNum, Integer pageSize, TbBrand brand) {
        //设置分页
        PageHelper.startPage(pageNum,pageSize);
        //设置查询条件
        Example example = new Example(TbBrand.class);
        //查询条件对象
        Example.Criteria criteria = example.createCriteria();

        /**
         * -- 根据品牌名称、首字母模糊条件查询并根据分页信息进行分页
         * select * from tb_brand where name like '%?%' and first_char=?  limit ?,?
         */

        //根据首字母
        if(StringUtils.isNotBlank(brand.getFirstChar())){
            criteria.andEqualTo("firstChar",brand.getFirstChar());
        }

        //根据名称模糊查询
        if(StringUtils.isNotBlank(brand.getName())){
            criteria.andLike("name","%"+brand.getName()+"%");
        }

        //查询
        List<TbBrand> list = brandMapper.selectByExample(example);
        //4、返回分页信息对象
        return PageInfo.of(list);
    }
}
