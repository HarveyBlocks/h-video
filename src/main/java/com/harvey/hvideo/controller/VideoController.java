package com.harvey.hvideo.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.harvey.hvideo.pojo.dto.UserDTO;
import com.harvey.hvideo.pojo.dto.VideoDTO;
import com.harvey.hvideo.pojo.entity.Video;
import com.harvey.hvideo.pojo.vo.Null;
import com.harvey.hvideo.pojo.vo.Result;
import com.harvey.hvideo.pojo.vo.ScrollResult;
import com.harvey.hvideo.Constants;
import com.harvey.hvideo.service.VideoService;
import com.harvey.hvideo.util.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
@RestController
@Slf4j
@RequestMapping("/video")
@Api(tags = "视频")
public class VideoController {

    @Resource
    private VideoService videoService;


    @ApiOperation("保存视频")
    @PostMapping
    public Result<Null> saveVideo(@RequestBody Video video) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        video.setUserId(user.getId());
        // 保存视频
        try {
            boolean saved = videoService.save(video);
            if (saved) {
                // 发送Video
                videoService.sendVideoToFans(video.getId());
            } else {
                throw new RuntimeException("未能成功保存视频");
            }
        } catch (Exception e) {
            throw new RuntimeException("未能成功保存视频");
        }
        // 返回id
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<Video> viewVideo(@PathVariable("id")  Long id) {
        return new Result<>(videoService.viewVideo(id));
    }

    /**
     * 当前用户写的视频
     *
     * @param current 当前页码
     * @return 自己的视频信息
     */
    @ApiOperation("查询自己发布的视频")
    @GetMapping("/of/me")
    public Result<List<VideoDTO>> queryMyVideo(
            @RequestParam(value = "current", defaultValue = "1")
            @ApiParam("页码,[1,...),默认1") Integer current) {
        return new Result<>(videoService.queryMyVideo(current));
    }

    @ApiOperation("滚动分页查询关注的人发布的视频")
    @GetMapping("/of/follow")
    public Result<ScrollResult<VideoDTO>> followVideos(
            @RequestParam("lastId")
            @ApiParam("上一次查询的最后一个Video的标识,第一次传当前时间,后来的偏移量后端会传给你的, 下一次请求就再把这个标识传给后端")
            Long lastTimestamp,
            @RequestParam(value = "offset", defaultValue = "0")
            @ApiParam("偏移量,默认0,第一次传0或不传,后来的偏移量后端会传给你的, 下一次请求就再把这个偏移量传给后端")
            Integer offset) {
        return new Result<>(videoService.queryFollowVideos(lastTimestamp,offset));
    }

    /**
     * 热门视频 TODO ES, Click排序
     *
     * @param current 当前页码
     * @return 热门视频集合
     */
    @ApiOperation("查询热门视频(关于点击量)")
    @GetMapping("/hot")
    public Result<List<VideoDTO>> queryHotVideo(
            @RequestParam(value = "current", defaultValue = "1")
            @ApiParam("页码,[1,...),默认1") Integer current) {
        return new Result<>(videoService.queryHotVideo(current));
    }
    /**
     * TODO ES, 依据tittle排序
     *
     * @param current 当前页码
     * @return 热门视频集合
     */
    @ApiOperation("查询热门视频(关于点击量)")
    @GetMapping("/search")
    public Result<List<VideoDTO>> queryVideoByTittle(
            @RequestParam(value = "current", defaultValue = "1")
            @ApiParam("页码,[1,...),默认1") Integer current,
            @RequestParam(value = "tittle", defaultValue = "1")
            @ApiParam("视频标题") String tittle) {
        videoService.saveSearchHistory(tittle);
        return new Result<>(videoService.queryVideoByTittle(current,tittle));
    }

    @ApiOperation("查询某用户的视频")
    @GetMapping("/of/user")
    public Result<List<VideoDTO>> queryVideoByUserId(
            @RequestParam(value = "current", defaultValue = "1")
            @ApiParam("页码,[1,...),默认1") Integer current,
            @RequestParam("id") @ApiParam("用户id") Long id) {
        // 根据用户查询
        Page<Video> page = videoService.query()
                .eq("user_id", id).page(new Page<>(current, Constants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Video> records = page.getRecords();
        return new Result<>(records
                .stream().map(VideoDTO::new).collect(Collectors.toList()));
    }


}
