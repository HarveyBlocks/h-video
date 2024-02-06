package com.harvey.hvideo.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.harvey.hvideo.pojo.entity.VideoComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-04 12:20
 */
@Mapper
public interface VideoCommentMapper  extends BaseMapper<VideoComment> {
}
