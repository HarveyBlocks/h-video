package com.harvey.hvideo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.harvey.hvideo.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户映射
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:14
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
