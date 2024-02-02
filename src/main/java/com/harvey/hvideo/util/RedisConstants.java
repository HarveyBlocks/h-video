package com.harvey.hvideo.util;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "video:login:code:";
    public static final Long LOGIN_CODE_TTL = 3*60L;
    public static final String LOGIN_USER_KEY = "video:login:token:";
    public static final Long LOGIN_USER_TTL = 2400*60*60L;

    public static final Long CACHE_NULL_TTL = 2L;
    public static final String FEED_KEY = "video:feed:";
    public static final String USER_SIGN_KEY = "video:sign:";

    public static final String FOLLOWED_KEY = "video:follow:concern:";//关注列表
    public static final String FOLLOWED_INBOX_KEY = "video:follow:inbox:";//粉丝列表
    public static final String USER_LOCK_KEY = "video:lock:user:";
    public static final long LOCK_TTL = 6 * 60L;
}
