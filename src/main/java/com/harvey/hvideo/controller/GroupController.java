package com.harvey.hvideo.controller;

import com.harvey.hvideo.pojo.dto.UserDto;
import com.harvey.hvideo.pojo.vo.Result;
import com.harvey.hvideo.service.GroupService;
import com.harvey.hvideo.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-12 16:52
 */
@Controller
@RequestMapping("/group")
@Api(tags = "群聊")
public class GroupController {
    @Resource
    private GroupService groupService;
    @Resource
    private UserService userService;
    /**00000000
     * 查询群聊成员
     *
     * @return boolean 关注了true,否则false
     */
    @ApiOperation("查询成员")
    @GetMapping("/{id}")
    public Result<Set<UserDto>> member(@PathVariable("id") Long groupId) {
        Set<Long> userIds = groupService.membersFromRedis(groupId);
        Set<UserDto> userDtos = userService.listByIds(userIds).stream().map(UserDto::new).collect(Collectors.toSet());
        return new Result<>(userDtos);
    }
}
