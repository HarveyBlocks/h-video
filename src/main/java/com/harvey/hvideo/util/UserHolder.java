package com.harvey.hvideo.util;

import com.harvey.hvideo.exception.UnauthorizedException;
import com.harvey.hvideo.pojo.dto.UserDto;

/**
 * 将用户信息存在ThreadLocal,方便取用
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:12
 */
public class UserHolder {
    private static final ThreadLocal<UserDto> TL = new ThreadLocal<>();

    public static void saveUser(UserDto user){
        TL.set(user);
    }

    public static UserDto getUser(){
        return TL.get();
    }

    public static void removeUser(){
        TL.remove();
    }
    public static Long currentUserId(){
        try {
            return TL.get().getId();
        } catch (NullPointerException e) {
            throw new UnauthorizedException("未登录");
        }
    }
}
