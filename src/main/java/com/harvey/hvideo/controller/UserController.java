package com.harvey.hvideo.controller;

import com.harvey.hvideo.exception.ResourceNotFountException;
import com.harvey.hvideo.pojo.dto.LoginFormDTO;
import com.harvey.hvideo.pojo.dto.RegisterFormDTO;
import com.harvey.hvideo.pojo.dto.UserDTO;
import com.harvey.hvideo.pojo.entity.User;
import com.harvey.hvideo.pojo.vo.Null;
import com.harvey.hvideo.pojo.vo.Result;
import com.harvey.hvideo.service.UploadService;
import com.harvey.hvideo.service.UserService;
import com.harvey.hvideo.util.Constants;
import com.harvey.hvideo.util.RedisConstants;
import com.harvey.hvideo.util.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
@Slf4j
@RestController
@Api(tags = "用户登录校验")
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    @ApiOperation("发送验证码")
    public Result<Null> sendCode(@RequestParam("phone") String phone) {
        // 发送短信验证码并保存验证码
        String code = userService.sendCode(phone);
        if (code == null) {
            return Result.fail("手机号不合法");
        }

        //session.setAttribute(CODE_SESSION_KEY,code);
        //session.setAttribute(PHONE_SESSION_KEY,phone);
        // 记得设置有效期
        stringRedisTemplate.opsForValue()
                .set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        return Result.ok();
    }

    /**
     * 登录功能
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    @ApiOperation("登录")
    public Result<Null> login(@RequestBody @ApiParam("需要用户登录的Json,密码和验证码二选一")
                              LoginFormDTO loginForm, HttpServletResponse response) {
        //实现登录功能
        // System.out.println(result);
        String token = userService.chooseLoginWay(loginForm);
        response.setHeader(Constants.AUTHORIZATION_HEADER, token);
        return Result.ok();
    }

    /**
     * 注册功能
     *
     * @param registerForm 注册参数，包含手机号、密码
     */
    @PostMapping("/register")
    @ApiOperation("注册")
    public Result<Null> register(@RequestBody @ApiParam("需要用户注册的Json,使用密码")
                                 RegisterFormDTO registerForm, HttpServletResponse response) {
//        System.err.println("hi");
        //实现注册功能
        String token = userService.register(registerForm);
        response.setHeader(Constants.AUTHORIZATION_HEADER, token);
        return Result.ok();
    }


    /**
     * 登出功能
     *
     * @return 无
     */
    @ApiOperation("登出")
    @PostMapping("/logout")
    public Result<Null> logout(HttpServletResponse response) {
        String tokenKey = RedisConstants.LOGIN_USER_KEY + UserHolder.currentUserId();
        stringRedisTemplate.delete(tokenKey);
        UserHolder.removeUser();// Interceptor会做,多此一举
        response.setStatus(401);
        return Result.ok();
    }

    @ApiOperation("获取当前登录的用户并返回")
    @GetMapping("/me")
    public Result<UserDTO> me() {
        // 获取当前登录的用户并返回
        return Result.ok(UserHolder.getUser(), UserDTO.class);
    }

    /**
     * UserController 根据id查询用户
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询用户")
    public Result<UserDTO> queryUserById(@PathVariable("id") Long userId) {
        UserDTO userDTO;
        try {
            userDTO = userService.queryUserByIdWithRedisson(userId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (userDTO == null) {
            throw new ResourceNotFountException("用户" + userId + "不存在");
        }
        return new Result<>(userDTO);

    }

    @GetMapping("/create")
    @ApiOperation(value = "测试用接口,生成虚假的User", notes = "生成100个虚假的用户,存入Redis")
    public Result<UserDTO> createUser() {
        for (int i = 0; i < 1; i++) {
            Map<String, String> map = new HashMap<>();
            int token = i + 10000;
            System.out.println(token);
            String key = RedisConstants.LOGIN_USER_KEY + token;
            map.put("id", String.valueOf(token));
            map.put("nickName", User.DEFAULT_NICKNAME);
            map.put("icon", "");
            stringRedisTemplate.opsForHash().putAll(key, map);
        }
        return Result.ok(new UserDTO(1, 1L, "nickName", "icon"), UserDTO.class);
    }

    @Resource
    private UploadService uploadService;


    @ApiOperation(value = "更新用户信息", notes = "没有更新的部分就传null或空字符串,不用传ID")
    @PutMapping("/update")
    public Result<Null> update(@RequestBody @ApiParam("需要用户注册的Json,使用密码")
                               UserDTO userDTO,
                               @ApiParam(hidden = true) HttpServletRequest request) {
        // 删除原有头像
        if (userDTO.getIcon() != null && !userDTO.getIcon().isEmpty()) {
            uploadService.deleteFile(Constants.IMAGE_UPLOAD_DIR, UserHolder.getUser().getIcon());
        }
        userService.updateUser(userDTO, request.getHeader(Constants.AUTHORIZATION_HEADER));
        return null;
    }


}