package com.hmall.searchservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import java.io.IOException;
@Slf4j
public class EsConnectConfig {
    @Bean
    public static void connect(ElasticsearchTask task) throws IOException {
        //建立连接
        RestHighLevelClient restHighLevelClient= new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.37.130:9100")
        ));
task.doSomething(restHighLevelClient);
        try {
            restHighLevelClient.close();
        } catch (IOException e) {
            log.error("关闭连接异常");

        }
    }
}