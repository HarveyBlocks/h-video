package com.harvey.hvideo.controller;


import com.harvey.hvideo.pojo.dto.MessageDto;
import com.harvey.hvideo.pojo.dto.RecordDto;
import com.harvey.hvideo.pojo.vo.Null;
import com.harvey.hvideo.pojo.vo.Result;
import com.harvey.hvideo.service.SessionRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 14:35
 */
@Slf4j
@RestController
@Api(tags = "用户会话记录")
@RequestMapping("/session")
public class SessionRecordController {
    @Resource
    private SessionRecordService sessionRecordService;
    @GetMapping("/{id}")
    @ApiOperation("获取用户会话列表, 参数是当前用户")
    public Result<List<RecordDto>> searchSessionRecord(@PathVariable("id") long id) {
        return new Result<>(sessionRecordService.searchSessionRecord(id));
    }

    @GetMapping("/session/{id}")
    @ApiOperation("获取用户会话")
    public Result<List<MessageDto>> viewRecord(@PathVariable("id") long id) {
        return new Result<>(sessionRecordService.viewRecord(id));
    }

    @PostMapping("/create")
    @ApiOperation("创建会话")
    public Result<Null> createSession(@RequestParam("from") long from,@RequestParam("to") long to) {
        sessionRecordService.createSessionIfNeeded(from,to);
        return Result.ok();
    }

    @PostMapping("/chat")
    @ApiOperation("会话")
    public Result<Null> chat(@RequestBody String command) {
       // TODO拓展
        return Result.ok();
    }
}
