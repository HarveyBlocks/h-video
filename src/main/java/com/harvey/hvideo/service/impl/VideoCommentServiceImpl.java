package com.harvey.hvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.hvideo.Constants;
import com.harvey.hvideo.dao.VideoCommentMapper;
import com.harvey.hvideo.pojo.dto.VideoCommentDTO;
import com.harvey.hvideo.pojo.entity.User;
import com.harvey.hvideo.pojo.entity.VideoComment;
import com.harvey.hvideo.service.UserService;
import com.harvey.hvideo.service.VideoCommentService;
import com.harvey.hvideo.service.VideoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-04 12:21
 */
@Service
public class VideoCommentServiceImpl extends ServiceImpl<VideoCommentMapper, VideoComment> implements VideoCommentService {
    @Resource
    private UserService userService;

    @Override
    public List<VideoCommentDTO> queryComments(Integer current, Integer videoId, Integer parentId) {
        // 获取登录用户
        BaseMapper<VideoComment> baseMapper = this.getBaseMapper();
        List<VideoComment> records = baseMapper.selectPage(
                new Page<>(current, Constants.MAX_PAGE_SIZE),
                new LambdaQueryWrapper<VideoComment>()
                        .eq(VideoComment::getVideoId, videoId)
                        .eq(parentId != 0, VideoComment::getParentId, parentId)
        ).getRecords();
        final String parentNickName;
        if (parentId != 0) {
            parentNickName = userService.getById(parentId).getNickName();
        } else {
            parentNickName = null;
        }
        return records.stream()
                .map((vc) -> {
                    User user = userService.getById(vc.getUserId());
                    return new VideoCommentDTO(vc, user, parentNickName);
                }).collect(Collectors.toList());
    }

    @Resource
    private VideoService videoService;

    @Override
    @Transactional
    public boolean saveComment(VideoComment videoComment) {
        if (videoComment==null||videoComment.getVideoId()==null){
            return false;
        }
        boolean update = videoService.update()
                .setSql("comments = comments + 1").eq("id", videoComment.getVideoId()).update();
        return update && this.save(videoComment);
    }
}
