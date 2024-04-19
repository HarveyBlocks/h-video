package com.harvey.hvideo.test.utils;

import com.harvey.hvideo.test.UserTest;

import java.util.Random;

/**
 * 电话号码随机生成
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-17 19:58
 */
public class Phone {

/*    public static void main(String[] args) {
        output(UserTest.COUNT, YD);
    }*/


    //移动电话号码前三位
    public static final String[] YD = {"134", "135", "136", "137", "138", "139", "150", "151", "152", "157", "158", "159", "180", "181", "182", "183", "184", "185", "174", "192", "178",};
    //电信号码前三位
    public static final String[] DX = {"133", "149", "153", "173", "177", "180", "181", "189", "199"};
    //联通号码前三位
    public static final String[] LT = {"130", "131", "132", "145", "155", "156", "166", "171", "175", "176", "185", "186", "166"};

    public static void output(int count, String[] var) {
        for (int i = 0; i < count; i++) {
            System.out.println(create(var));
        }
    }
    public static void output(int count) {
        for (int i = 0; i < count; i++) {
            System.out.println(create());
        }
    }


    public static String create(String[] var) {
        //从电信号码规则里面随机一个号码前三位
        StringBuilder stringBuilder = new StringBuilder(11);
        stringBuilder.append(var[UserTest.RANDOM.nextInt(var.length)]);
        for (int j = 0; j < 8; j++) {
            stringBuilder.append(UserTest.RANDOM.nextInt(10));
        }
        return stringBuilder.toString();
    }
    public static String create() {
        String[] var = new String[0];
        switch (UserTest.RANDOM.nextInt(3)) {
            case 0:
                var = YD;
                break;
            case 1:
                var = DX;
                break;
            case 2:
                var = LT;
                break;
        }
        return create(var);
    }
}