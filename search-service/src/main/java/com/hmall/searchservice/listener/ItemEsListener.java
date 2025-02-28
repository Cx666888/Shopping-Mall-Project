package com.hmall.searchservice.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.heima.hmapi.client.ItemClient;
import com.heima.hmapi.dto.ItemDTO;
import com.hmall.common.constants.ItemConstants;
import com.hmall.searchservice.config.EsConnectConfig;
import com.hmall.searchservice.domain.po.ItemDoc;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ItemEsListener {
    private final  ItemClient itemClient;
   public  String getJsonStr(Long id){
       ItemDTO itemDTO = itemClient.queryItemById(id);
       if(null==itemDTO){
           return "没有获取该商品信息";
       }
       ItemDoc itemDoc = BeanUtil.copyProperties(itemDTO, ItemDoc.class);
       itemDoc.setUpdateTime(LocalDateTime.now());
       String jsonStr = JSONUtil.toJsonStr(itemDoc);
       return jsonStr;
   }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name= ItemConstants.ITEM_INDEX_QUEUE_NAME,durable = "true"),
            exchange = @Exchange(value = ItemConstants.ITEM_NAME,type = ExchangeTypes.DIRECT),
            key=ItemConstants.ITEM_INDEX_KEY
    ))
    public void ListenItemIndex(Long id) throws IOException {
        EsConnectConfig.connect(client->{
            String jsonStr = getJsonStr(id);
            IndexRequest request = new IndexRequest("item").id(String.valueOf(id));
            request.source(jsonStr, XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);
            System.out.println("我检测到MQ给我发送商品新增的消息了，我被触发开始存入消息"+jsonStr);
        });

    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name= ItemConstants.ITEM_UPDATE_QUEUE_NAME,durable = "true"),
            exchange = @Exchange(value = ItemConstants.ITEM_NAME,type = ExchangeTypes.DIRECT),
            key=ItemConstants.ITEM_UPDATE_KEY
    ))
    public void ListenItemUpdate(Long id) throws IOException {
       EsConnectConfig.connect(client->{
           String jsonStr = getJsonStr(id);
           IndexRequest request = new IndexRequest("item").id(String.valueOf(id));
           request.source(jsonStr, XContentType.JSON);
           client.index(request, RequestOptions.DEFAULT);
           System.out.println("我检测到MQ给我发送商品修改的消息了，我被触发开始存入消息"+jsonStr);
       });

    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name= ItemConstants.ITEM_DELETE_QUEUE_NAME,durable = "true"),
            exchange = @Exchange(value = ItemConstants.ITEM_NAME,type = ExchangeTypes.DIRECT),
            key=ItemConstants.ITEM_DELETE_KEY
    ))
    public void ListenItemDelete(Long id) throws IOException {
       EsConnectConfig.connect(client->{
           DeleteRequest request = new DeleteRequest("items", String.valueOf(id));
           client.delete(request,RequestOptions.DEFAULT);
           System.out.println("我检测到MQ给我发送商品删除的消息了，商品id:"+id);
       });

    }
}
