package com.harvey.hvideo.test.utils;

import com.harvey.hvideo.test.UserTest;

/**
 * 字母组成的字符串生成器
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-17 20:12
 */
public class LitterStringCreator {
/*    public static void main(String[] args) {
        output(UserTest.COUNT,12, 0.8);
    }*/

    public static void output(int count, int len, double percent) {
        for (int i = 0; i < count; i++) {
            System.out.println(create(len,percent));
        }
    }

    public static String create(int len, double percent) {
        StringBuilder stringBuilder = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            stringBuilder.append(randomChar(percent));
        }
        return stringBuilder.toString();
    }

    /**
     * @param percent 越接近1, 小写越多, 越接近0, 大写越多
     */
    private static char randomChar(double percent) {
        return UserTest.RANDOM.nextDouble() > percent ? randomUpChar() : randomLowChar();
    }

    private static char randomUpChar() {
        return (char) (UserTest.RANDOM.nextInt(26) + 65);
    }

    private static char randomLowChar() {
        return (char) (UserTest.RANDOM.nextInt(26) + 97);
    }
}
