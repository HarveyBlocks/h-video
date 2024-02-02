package com.harvey.hvideo.config;

import com.harvey.hvideo.interceptor.AuthorizeInterceptor;
import com.harvey.hvideo.interceptor.ExpireInterceptor;
import com.harvey.hvideo.interceptor.LoginInterceptor;
import com.harvey.hvideo.properties.AuthProperties;
import com.harvey.hvideo.util.JwtTool;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * MVC的配置类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-01-03 14:12
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private AuthProperties authProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JwtTool jwtTool;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(
                new ExpireInterceptor(stringRedisTemplate,jwtTool));


        List<String> excludePaths = authProperties.getExcludePaths();
        if (excludePaths == null) {
            excludePaths = Collections.emptyList();
        }
        List<String> includePaths = authProperties.getIncludePaths();
        if (includePaths == null) {
            includePaths = Collections.emptyList();
        }

        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns(includePaths);

        registry.addInterceptor(new AuthorizeInterceptor())
                .excludePathPatterns(includePaths);
    }
}
