package com.harvey.hvideo.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-01-30 22:19
 */
public class TimeUtil {
    public static Long toMillion(LocalDateTime time) {
        return time.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }
}
