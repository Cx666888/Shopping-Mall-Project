package com.hmall.searchservice.config;
import org.elasticsearch.client.RestHighLevelClient;
import java.io.IOException;
public interface ElasticsearchTask {
    void doSomething( RestHighLevelClient restHighLevelClient) throws IOException;
}
