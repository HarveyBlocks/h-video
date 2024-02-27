package com.harvey.hvideo.controller;


import com.harvey.hvideo.pojo.dto.UserDTO;
import com.harvey.hvideo.pojo.vo.Null;
import com.harvey.hvideo.pojo.vo.Result;
import com.harvey.hvideo.service.FollowService;
import com.harvey.hvideo.util.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
@RestController
@RequestMapping("/follow")
@Api(tags = "关注")
public class FollowController {
    @Resource
    private FollowService followService;


    /**
     * 查询是否有关注
     *
     * @return boolean 关注了true,否则false
     */
    @ApiOperation("是否关注")
    @GetMapping("or/not/{id}")
    public Result<Boolean> isFollowed(@PathVariable("id") Long authorId) {
        return new Result<>(followService.isFollowed(authorId));
    }


    /**
     * 关注用户
     *
     * @param canFollow 能否关注, 已关注就不能再关注, 将传入false,false就表示取关
     * @return ???
     */
    @PutMapping("{id}/{canFollow}")
    @ApiOperation("关注/取消关注")
    public Result<Null> follow(@PathVariable("id") @ApiParam("关注的发布者信息") Long authorId,
                               @PathVariable("canFollow") @ApiParam("是否已经关注,已经关注的将取消关注")
                               boolean canFollow) {
        followService.followOrCancel(authorId, canFollow);
        return Result.ok();
    }

    @GetMapping("common/{id}")
    @ApiOperation("查询共同关注列表")
    public Result<List<UserDTO>> follow(@PathVariable("id") Long authorId) {
        return new Result<>(followService.followInteraction(authorId, UserHolder.getUser().getId()));
    }

    @GetMapping("author/me")
    @ApiOperation("查询当前用户关注列表")
    public Result<List<UserDTO>> queryFollowList() {
        return new Result<>(followService.followList(UserHolder.getUser().getId()));
    }


    @GetMapping("fan/")
    @ApiOperation("分页查询粉丝列表")
    public Result<List<UserDTO>> queryFanList(
            @RequestParam("authorId") Long authorId,
            @RequestParam(value = "current", defaultValue = "1")
            @ApiParam("页码,[1,...),默认1") Integer current) {
        return new Result<>(followService.queryFanList(authorId,current));
    }


    @GetMapping("friend/me")
    @ApiOperation("查询当前用户的好友列表")
    public Result<List<UserDTO>> queryFriendList() {
        return new Result<>(followService.friendList(UserHolder.getUser().getId()));
    }

}
