package com.harvey.hvideo.controller;

import com.harvey.hvideo.pojo.dto.VideoCommentDto;
import com.harvey.hvideo.pojo.entity.VideoComment;
import com.harvey.hvideo.pojo.vo.Null;
import com.harvey.hvideo.pojo.vo.Result;
import com.harvey.hvideo.service.VideoCommentService;
import com.harvey.hvideo.util.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-04 12:23
 */
@Slf4j
@RequestMapping("/video/comment")
@Api(tags = "视频评论")
@RestController
public class VideoCommentController {
    @Resource
    private VideoCommentService videoCommentService;


    @ApiOperation("写评论")
    @PostMapping
    public Result<Null> saveComment(@RequestBody VideoComment videoComment) {
        // 获取登录用户
        Long userId = UserHolder.currentUserId();
        videoComment.setUserId(userId);
        //
        boolean saved;
        try {
            saved = videoCommentService.saveComment(videoComment);
        } catch (Exception e) {
            throw new RuntimeException("未能成功保存评论");
        }
        if (!saved) {
            throw new RuntimeException("未能成功保存评论");
        }
        // 返回id
        return Result.ok();
    }



    @ApiOperation("查评论")
    @GetMapping("/of")
    public Result<List<VideoCommentDto>> queryComments(
            @RequestParam(value = "current", defaultValue = "1")
            @ApiParam(value = "页码,[1,...),默认1", defaultValue = "1") Integer current,
            @RequestParam(value = "videoId")
            @ApiParam("视频id") Integer videoId,
            @RequestParam(value = "parentId", defaultValue = "0")
            @ApiParam(value = "被回复的评论id", defaultValue = "0") Integer parentId
    ) {
        // 获取当前页数据
        return new Result<>(videoCommentService.queryComments(current, videoId, parentId));
    }
}
