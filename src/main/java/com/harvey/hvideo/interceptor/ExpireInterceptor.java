package com.harvey.hvideo.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.harvey.hvideo.Constants;
import com.harvey.hvideo.pojo.dto.UserDto;
import com.harvey.hvideo.util.JwtTool;
import com.harvey.hvideo.util.RedisConstants;
import com.harvey.hvideo.util.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录拦截器,会从Redis中查出用户的信息, 查到了就存入ThreadLocal
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-01-03 13:32
 */
public class ExpireInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtTool jwtTool;

    public ExpireInterceptor(StringRedisTemplate stringRedisTemplate, JwtTool jwtTool) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtTool = jwtTool;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 进入controller之前进行登录校验

//        System.err.println("1");
        String id;
        // 获取请求头中的token
        String token = request.getHeader(Constants.AUTHORIZATION_HEADER);//依据前端的信息
        if (token == null || token.isEmpty()) {
            id = request.getRemoteAddr();
        }else {
            id = jwtTool.parseToken(token).toString();
        }
//        System.err.println("2");

        // 获取user数据
        String tokenKey = RedisConstants.LOGIN_USER_KEY + id;
        Map<Object, Object> userFieldMap = stringRedisTemplate.opsForHash().entries(tokenKey);
        if (userFieldMap.isEmpty()) {
            // entries不会返回null
            // 用户不存在,就是游客,也给他限个流
            stringRedisTemplate.opsForHash().put(tokenKey,"time",Constants.RESTRICT_REQUEST_TIMES);
            userFieldMap.put("time","20");
        }

//        System.err.println("3");

        // 更新时间
        if (RedisConstants.LOGIN_USER_TTL != -1L) {
            stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        }

        String time = (String) userFieldMap.get("time");
        if ("0".equals(time)) {
            return false;
        } else {
            stringRedisTemplate.opsForHash().increment(tokenKey, "time", -1);
        }
        userFieldMap.remove("time");
        if (userFieldMap.isEmpty()){
            // 现在是游客的可以走了
            return true;
        }
        // 第三个参数: 是否忽略转换过程中产生的异常
        UserDto user = BeanUtil.fillBeanWithMap(userFieldMap, new UserDto(), false);


//        System.err.println("4");

        // 保存到ThreadLocal
        UserHolder.saveUser(user);
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        String id;
        try {
            id = UserHolder.currentUserId().toString();
        } catch (Exception e) {
            id = request.getRemoteAddr();
        }
        String tokenKey = RedisConstants.LOGIN_USER_KEY + id;
        stringRedisTemplate.opsForHash().increment(tokenKey, "time", 1);

        // 完成Controller之后移除UserHolder, 以防下一次用这条线程的请求获取到不属于它的用户信息
        UserHolder.removeUser();
    }
}
