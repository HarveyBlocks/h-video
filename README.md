# 第四次作业

>项目名: hvideo

## 2.1

> 发烧中

### 用户

#### 用户表

```mysql
CREATE TABLE `tb_user` (
  `id` bigint unsigned NOT NULL COMMENT '主键',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '密码，加密存储',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '昵称，默认是用户id',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '人物头像',
  `role` int DEFAULT '1' COMMENT '用户权限',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uniqe_key_phone` (`phone`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=COMPACT COMMENT='用户表'
```

#### 用户认证

通过俩拦截器ExpireInterceptor和LoginInterceptor共同完成

对于`hmall.jks`,是从别的项目拷贝过来的,使用了MD5加密

#### 用户权限

见enums.Role

## 2.2

> 发烧中

### 对于击穿,穿透和雪崩

主要体现在UserController的queryUserById,其他的地方我应该不会做这个了

没啥实际意义,单纯就是写一下

至于别的可能用到的地方, 太累了不想写

虽然可以提出来做一个工具类(我确实有一套),但我就是不想写😕,连复制也不想复制

### 对于授权

~~我就是不喜欢SpringSecurity帮我封装好, 我就是喜欢自己写~~

反正到最后都是要**自定义认证**,**自定义登录**啥的,**自定义授权**,不如一开始就用自己写的
而且会一直跳到自带的静态资源,老慢了,很烦,也不想写html.
而且不想准备role-permission表,烦