package com.harvey.hvideo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 启动类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:05
 */
@MapperScan(basePackages = "com.harvey.hvideo.dao")
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableScheduling
public class HVideoApplication {
    public static void main(String[] args) {
        SpringApplication.run(HVideoApplication.class, args);
    }
}
