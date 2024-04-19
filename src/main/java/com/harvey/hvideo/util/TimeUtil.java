package com.harvey.hvideo.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 时间工具
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-01-30 22:19
 */
public class TimeUtil {
    public static Long toMillion(LocalDateTime time) {
        if(time==null){
            return null;
        }
        return time.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    public static LocalDateTime toTime(Long timestamp) {
        if (timestamp==null){
            return null;
        }
        Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneOffset.of("+8"));
    }
}
