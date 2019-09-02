package com.pinyougou.mapper;

import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.ids.DeleteByIdsMapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

@RegisterMapper
public interface BaseMapper<T> extends Mapper<T>,InsertListMapper<T>,DeleteByIdsMapper<T> {
}
