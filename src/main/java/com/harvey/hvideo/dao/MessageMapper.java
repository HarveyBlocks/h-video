package com.harvey.hvideo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.harvey.hvideo.pojo.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-12 13:59
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
