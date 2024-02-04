package com.harvey.hvideo.advice;

import com.harvey.hvideo.pojo.vo.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;

/**
 * 面向切面增强controller解决swagger对已经封装的Result不会扫描到Model的问题.但是对status code的设置很难.所以没用
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 17:31
 */
//@ControllerAdvice
@Deprecated
public class RestfulResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 过滤Controller
        Method method = returnType.getMethod();
        if (method==null) {
            return false;
        }
        if("getDocumentation".equals(method.getName())){
            return false;
        }
        if ("swaggerResources".equals(method.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
//        System.out.println("-----------------beforeBodyWrite-----------------");
//        System.out.println(body);
//        if (response.setStatusCode());
        return new Result<>(body);
    }
}
