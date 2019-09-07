package com.pinyougou.sellergoods.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService extends BaseService<TbSpecification> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param specification 搜索条件
     * @return 分页信息
     */
    PageInfo<TbSpecification> search(Integer pageNum, Integer pageSize, TbSpecification specification);

    /**
     * 新增
     * @param specification 实体
     * @return 操作结果
     */
    void addSpecification(Specification specification);

    /**
     * 根据主键查询
     * @param id 主键
     * @return 规格vo
     */
    Specification findSpecificationById(Long id);

    /**
     * 修改
     * @param specification 实体
     * @return 操作结果
     */
    void updateSpecification(Specification specification);

    /**
     * 根据主键数组批量删除
     * @param ids 主键数组
     * @return 实体
     */
    void deleteSpecificationByIds(Long[] ids);

    /**
     * 查询所有规格列表；格式为：[{id:'1',text:'屏幕尺寸'},{id:'2',text:'机身大小'}]
     * @return 规格列表；格式为：[{id:'1',text:'屏幕尺寸'},{id:'2',text:'机身大小'}]
     */
    List<Map<String,String>> selectOptionList();
}
