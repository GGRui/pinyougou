package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.sellergoods.service.SpecificationService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Specification;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl extends BaseServiceImpl<TbSpecification> implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageInfo<TbSpecification> search(Integer pageNum, Integer pageSize, TbSpecification specification) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbSpecification.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        if (StringUtils.isNotBlank(specification.getSpecName())) {
            criteria.andLike("specName", "%" + specification.getSpecName() + "%");
        }

        List<TbSpecification> list = specificationMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void addSpecification(Specification specification) {
        //保存规格；通过mapper在操作之后会对主键进行回填到实体对象
        add(specification.getSpecification());

        //保存规格选项列表
        //判断列表是否为空、个数大小是否大于0
        if (specification.getSpecificationOptionList() != null && specification.getSpecificationOptionList().size() > 0) {
            for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {
                //重新设置规格id
                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
            }
            //把得到的选项列表保存
            specificationOptionMapper.insertList(specification.getSpecificationOptionList());
        }
    }

    @Override
    public Specification findSpecificationById(Long id) {

        Specification specification = new Specification();

        //规格
        specification.setSpecification(findOne(id));

        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(id);

        //查询规格选项列表
        List<TbSpecificationOption> list = specificationOptionMapper.select(param);
        specification.setSpecificationOptionList(list);

        return specification;
    }

    @Override
    public void updateSpecification(Specification specification) {
        //1.根据规格id更新规格
        update(specification.getSpecification());

        //2.根据规格id删除规格选项
        /**
         * sql --> delete from tb_specification_option where spec_id=?
         */
        TbSpecificationOption param = new TbSpecificationOption();
        param.setSpecId(specification.getSpecification().getId());

        specificationOptionMapper.delete(param);

        //3.将规格选项列表保存到数据库中
        if (specification.getSpecificationOptionList() != null && specification.getSpecificationOptionList().size() > 0){
            for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {

                //设置id
                tbSpecificationOption.setSpecId(specification.getSpecification().getId());
            }
            specificationOptionMapper.insertList(specification.getSpecificationOptionList());
        }
    }

    @Override
    public void deleteSpecificationByIds(Long[] ids) {
        //根据规格id批量删除规格
        //delete from tb_specification where id in(?,?...)
        deleteByIds(ids);

        //根据规格id批量删除规格选项
        //delete from tb_specification_option where spec_id in(?,?...)
        Example example = new Example(TbSpecificationOption.class);

        example.createCriteria().andIn("specId", Arrays.asList(ids));
        specificationOptionMapper.deleteByExample(example);

    }

    @Override
    public List<Map<String, String>> selectOptionList() {
        return specificationMapper.selectOptionList();
    }

}
