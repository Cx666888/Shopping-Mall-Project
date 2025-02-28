package com.hmall.searchservice.Service;

import com.hmall.common.domain.PageDTO;
import com.hmall.searchservice.domain.po.Item;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.searchservice.domain.po.ItemDoc;
import com.hmall.searchservice.domain.query.ItemPageQuery;
import com.hmall.searchservice.domain.vo.CategoryAndBrandVo;

import java.io.IOException;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author 陈雪
 * @since 2025-02-23
 */
public interface ISearchService extends IService<Item> {

    PageDTO<ItemDoc> EsSearch(ItemPageQuery query) throws IOException;

    CategoryAndBrandVo getFilters(ItemPageQuery query) throws IOException;
}
