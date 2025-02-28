package com.hmall.searchservice.Mapper;

import com.hmall.searchservice.domain.po.Item;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author 陈雪
 * @since 2025-02-23
 */
@Mapper
public interface SearchMapper extends BaseMapper<Item> {

}
