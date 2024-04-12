package com.harvey.hvideo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.harvey.hvideo.pojo.dto.VideoCommentDto;
import com.harvey.hvideo.pojo.entity.VideoComment;

import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-04 12:19
 */
public interface VideoCommentService extends IService<VideoComment> {
    List<VideoCommentDto> queryComments(Integer current, Integer videoId, Integer parentId);

    boolean saveComment(VideoComment videoComment);
}
