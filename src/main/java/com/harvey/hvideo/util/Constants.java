package com.harvey.hvideo.util;

import java.util.Set;

/**
 * 系统常量类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 15:17
 */
public class Constants {
    public static final String AUTHORIZATION_HEADER = "authorization";
    public static final String VIDEO_UPLOAD_DIR = "C:/Users/27970/Desktop/IT/nginx-1.18.0/html/hvideo/videos/";
    public static final String IMAGE_UPLOAD_DIR = "C:/Users/27970/Desktop/IT/nginx-1.18.0/html/hvideo/imgs/";
    public static final String RESTRICT_REQUEST_TIMES = "20";
    public static final Set<String> ROOT_AUTH_URI = Set.of("/user/create");
    public static final Integer MAX_PAGE_SIZE = 10;
    public static final Integer DEFAULT_PAGE_SIZE = 5;
}
