package com.pinyougou.vo;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

public class Specification implements Serializable{

    //规格
    private TbSpecification Specification;
    //规格列表选项
    private List<TbSpecificationOption> SpecificationOptionList;

    public TbSpecification getSpecification() {
        return Specification;
    }

    public void setSpecification(TbSpecification specification) {
        Specification = specification;
    }

    public List<TbSpecificationOption> getSpecificationOptionList() {
        return SpecificationOptionList;
    }

    public void setSpecificationOptionList(List<TbSpecificationOption> specificationOptionList) {
        SpecificationOptionList = specificationOptionList;
    }
}
