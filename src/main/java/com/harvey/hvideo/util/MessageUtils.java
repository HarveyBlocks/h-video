package com.harvey.hvideo.util;

import com.alibaba.fastjson.JSON;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 21:52
 */
public class MessageUtils {
    public static String getResultMessage(Object o){
        return JSON.toJSONString(o);
    }
    public static <T> T getMessage(String json,Class<T> type){
        return JSON.parseObject(json, type);
    }
}