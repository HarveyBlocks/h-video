package com.harvey.hvideo.interceptor;

import com.harvey.hvideo.Constants;
import com.harvey.hvideo.pojo.enums.Role;
import com.harvey.hvideo.util.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 授权拦截器
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-02 11:21
 */
public class AuthorizeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        if (Constants.ROOT_AUTH_URI.contains(request.getRequestURI())){
            if (UserHolder.getUser().getRole().intValue() != Role.ROOT.getValue()){
                System.out.println("yes");
                response.setStatus(401);
                return false;
            }
        }
        return true;
    }

}