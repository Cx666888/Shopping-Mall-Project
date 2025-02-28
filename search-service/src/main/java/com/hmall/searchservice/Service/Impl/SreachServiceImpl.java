package com.hmall.searchservice.Service.Impl;

import cn.hutool.json.JSONUtil;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.utils.CollUtils;
import com.hmall.searchservice.config.EsConnectConfig;
import com.hmall.searchservice.domain.po.Item;
import com.hmall.searchservice.Mapper.SearchMapper;
import com.hmall.searchservice.Service.ISearchService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.searchservice.domain.po.ItemDoc;
import com.hmall.searchservice.domain.query.ItemPageQuery;
import com.hmall.searchservice.domain.vo.CategoryAndBrandVo;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author 陈雪
 * @since 2025-02-23
 */
@Service
public class SreachServiceImpl extends ServiceImpl<SearchMapper, Item> implements ISearchService  {
    @Override
    public PageDTO<ItemDoc> EsSearch(ItemPageQuery query) throws IOException {
        PageDTO<ItemDoc> result = new PageDTO<>();
        EsConnectConfig.connect(Client -> {
            SearchRequest request = new SearchRequest("items");
            BoolQueryBuilder bool = QueryBuilders.boolQuery();
            //精准总数,超10000
            request.source().trackTotalHits(true);
            extracted(query, bool);
            // 2.2.高亮条件
            request.source().highlighter(
                    SearchSourceBuilder.highlight()
                            .field("name")
                            .preTags("<em>")
                            .postTags("</em>")
            );

            //分页
            request.source().from(query.from()).size(query.getPageSize());//获取页数
            //排序
            if (!"".equals(query.getSortBy())) {
                request.source().sort(query.getSortBy(), query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC);
            } else {
                request.source().sort("updateTime", query.getIsAsc() ? SortOrder.ASC : SortOrder.DESC);
            }
            /*
            GET /hotel/_search
{
  "query": {
    "function_score": {
      "query": {  .... }, // 原始查询，可以是任意条件
      "functions": [ // 算分函数
        {
          "filter": { // 满足的条件，品牌必须是Iphone
            "term": {
              "brand": "Iphone"
            }
          },
          "weight": 10 // 算分权重为2
        }
      ],
      "boost_mode": "multipy" // 加权模式，求乘积
    }
  }
}             * */
            //函数算分
            request.source().query(QueryBuilders.functionScoreQuery(bool,
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isAD", true),
                                    ScoreFunctionBuilders.weightFactorFunction(100))
                    }).boostMode(CombineFunction.MULTIPLY));
            // 3.发送请求
            SearchResponse response = Client.search(request, RequestOptions.DEFAULT);
            // 4.解析响应
            long value = response.getHits().getTotalHits().value;
            result.setTotal(value);
            result.setPages(value % query.getPageSize() == 0 ? value / query.getPageSize() : value / query.getPageSize() + 1);
            SearchHit[] hits = response.getHits().getHits();
            List<ItemDoc> list = new ArrayList<>();
            for (SearchHit hit : hits) {
                ItemDoc itemDoc = JSONUtil.toBean(hit.getSourceAsString(), ItemDoc.class);
                Map<String, HighlightField> hfs = hit.getHighlightFields();
                if (CollUtils.isNotEmpty(hfs)) {
                    // 5.1.有高亮结果，获取name的高亮结果
                    HighlightField hf = hfs.get("name");
                    if (hf != null) {
                        // 5.2.获取第一个高亮结果片段，就是商品名称的高亮值
                        String hfName = hf.getFragments()[0].string();
                        itemDoc.setName(hfName);
                    }
                }

                list.add(itemDoc);
            }
            result.setList(list);
        });
        return result;
    }

    private void extracted(ItemPageQuery query, BoolQueryBuilder bool) {
        if (query.getKey() != null && !"".equals(query.getKey())) {
            bool.must(QueryBuilders.matchQuery("name", query.getKey()));
        }
        //分类
        if (query.getCategory() != null && !"".equals(query.getCategory())) {
            bool.filter(QueryBuilders.termQuery("category", query.getCategory()));
        }
        // 2.3.品牌过滤
        if (query.getCategory() != null && !"".equals(query.getCategory())) {
            bool.filter(QueryBuilders.termQuery("brand", query.getBrand()));
        }
        // 2.4.价格过滤
        if (query.getMinPrice() != null && query.getMaxPrice() != null) {
            bool.filter(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()).lte(query.getMaxPrice()));
        }
    }

    @Override
    public CategoryAndBrandVo getFilters(ItemPageQuery query) throws IOException {
        CategoryAndBrandVo categoryAndBrandVo = new CategoryAndBrandVo();
        EsConnectConfig.connect(Client -> {
            List<String> categoryList = new ArrayList<>();
            List<String> brandList = new ArrayList<>();
            SearchRequest request = new SearchRequest("items");
            BoolQueryBuilder bool = QueryBuilders.boolQuery();
            extracted(query, bool);
            request.source().query(bool).size(0);
            request.source().aggregation(AggregationBuilders.terms("category_agg").field("category").size(10));
            request.source().aggregation(AggregationBuilders.terms("brand_agg").field("brand").size(10));
            SearchResponse response = Client.search(request, RequestOptions.DEFAULT);
            Aggregations aggregations = response.getAggregations();
            Terms categoryTerms = aggregations.get("category_agg");
            // 5.2.获取聚合中的桶
            List<? extends Terms.Bucket> buckets = categoryTerms.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                // 5.4.获取桶内key
                String category = bucket.getKeyAsString();
                categoryList.add(category);
            }
            Terms brandTerms = aggregations.get("brand_agg");
            // 5.2.获取聚合中的桶
            List<? extends Terms.Bucket> buckets1 = brandTerms.getBuckets();
            for (Terms.Bucket bucket : buckets1) {
                // 5.4.获取桶内key
                String brand = bucket.getKeyAsString();
                brandList.add(brand);
            }
            categoryAndBrandVo.setCategory(categoryList);
            categoryAndBrandVo.setBrand(brandList);
        });
        return categoryAndBrandVo;
    }
}
