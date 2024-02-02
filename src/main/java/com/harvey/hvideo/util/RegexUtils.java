package com.harvey.hvideo.util;

/**
 * @author 虎哥
 */
public class RegexUtils {
    /**
     * 是否是有效手机格式
     *
     * @param phone 要校验的手机号
     * @return true:有效，false：无效
     */
    public static boolean isPhoneEffective(String phone) {

        return match(phone, RegexPatterns.PHONE_REGEX);
        // PHONE_REGEX = ^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$
    }

    /**
     * 是否是有效邮箱格式
     *
     * @param email 要校验的邮箱
     * @return true:有效，false：无效
     */
    public static boolean isEmailEffective(String email) {
        return match(email, RegexPatterns.EMAIL_REGEX);
    }

    /**
     * 是否是有效验证码格式
     *
     * @param code 要校验的验证码
     * @return true:有效，false：无效
     */
    public static boolean isCodeEffective(String code) {
        return match(code, RegexPatterns.VERIFY_CODE_REGEX);
    }

    // 校验是否复合正则格式
    private static boolean match(String str, String regex) {
        if (str==null|| str.isEmpty()) {
            return false;
        }
        return str.matches(regex);
    }
}
