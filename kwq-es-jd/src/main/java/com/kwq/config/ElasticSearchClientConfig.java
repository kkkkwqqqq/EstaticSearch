package com.kwq.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//spring两步骤：
//1. 找对象
//2. 放到spring中待用
//3.如果是springboot 就先分析源码
// xxxAutoConfiguration xxxProperties
@Configuration   //将@Bean注入spring中
public class ElasticSearchClientConfig {
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client=new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1",9200,"http")));
                return client;
    }
}
