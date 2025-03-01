package com.hmall.searchservice.Controller;


import com.hmall.common.domain.PageDTO;
import com.hmall.searchservice.Service.ISearchService;
import com.hmall.searchservice.domain.po.ItemDoc;
import com.hmall.searchservice.domain.query.ItemPageQuery;
import com.hmall.searchservice.domain.vo.CategoryAndBrandVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author 陈雪
 * @since 2025-02-23
 */
@Api(tags = "商品搜索接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final ISearchService iSearchService;
    @ApiOperation("搜索商品")
    @GetMapping({"list"})
        public PageDTO<ItemDoc> search(ItemPageQuery query) throws IOException /*{
            // 分页查询
            Page<Item> result = itemService.lambdaQuery()
                    .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
                    .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
                    .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
                    .eq(Item::getStatus, 1)
                    .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
                    .page(query.toMpPage("update_time", false));
            // 封装并返回
            return PageDTO.of(result, ItemDTO.class);
        }*/
    {
        return iSearchService.EsSearch(query);
    }
    @ApiOperation("分类聚合接口")
    @PostMapping("/filters")
    public CategoryAndBrandVo getFilters(@RequestBody ItemPageQuery query) throws IOException {
        return iSearchService.getFilters(query);
    }

}


