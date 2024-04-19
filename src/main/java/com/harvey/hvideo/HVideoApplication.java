package com.harvey.hvideo;

import com.harvey.hvideo.util.IpTool;
import com.harvey.hvideo.util.TimeUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.ContextClosedEvent;

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
public class HVideoApplication implements ApplicationListener<ContextClosedEvent> {

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        IpTool.close();
        System.out.println(TimeUtil.toTime(event.getTimestamp())+", Ip文件已关闭");
    }


    public static void main(String[] args) {
        SpringApplication.run(HVideoApplication.class, args);
    }

}
