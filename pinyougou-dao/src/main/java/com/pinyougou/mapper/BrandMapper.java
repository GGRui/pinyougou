package com.pinyougou.mapper;

import com.pinyougou.pojo.TbBrand;



import java.util.List;

public interface BrandMapper extends BaseMapper<TbBrand>{


    List<TbBrand> queryAll();
}
