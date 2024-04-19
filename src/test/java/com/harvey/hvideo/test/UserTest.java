package com.harvey.hvideo.test;

import com.alibaba.fastjson.JSON;
import com.harvey.hvideo.pojo.dto.RegisterFormDto;
import com.harvey.hvideo.test.utils.LitterStringCreator;
import com.harvey.hvideo.test.utils.Phone;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.Supplier;

/**
 * 用户测试
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-17 20:13
 */
public class UserTest {
    public static final Random RANDOM = new Random(System.currentTimeMillis());
    public static final int COUNT = 200;


    /**
     * 准备用户登录数据
     */
    @Test
    void registerDtoCsvCreate() {
        outputJsons(() -> {
            String phone = Phone.create();
            String nickName = LitterStringCreator.create(12, 0.8);
            String password = LitterStringCreator.create(12, 0.6);
            String icon = "";
            return new RegisterFormDto(phone, password, nickName, icon);
        });
    }

    private static void outputJsons(Supplier<Object> supplier) {
        for (int i = 0; i < COUNT; i++) {
            String jsonString = JSON.toJSONString(supplier.get());
            System.out.println(jsonString);
        }
    }
}
