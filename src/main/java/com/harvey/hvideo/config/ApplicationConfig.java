package com.harvey.hvideo.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对上下文的设置
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-01-21 21:13
 */
@Configuration
public class ApplicationConfig {
    @Bean
    public RedissonClient redissonClient() {
        // 配置类
        Config config = new Config();
        // 添加Redis地址, 这里添加了单点的地址
        config.useSingleServer().setAddress("redis://centos:6379").setPassword("123456");
        // 也可以使用config.useClusterServers()添加集群地址
        return Redisson.create(config);
    }
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://centos:9200")
        ));
    }


}
