package com.harvey.hvideo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.harvey.hvideo.pojo.dto.MessageDto;
import com.harvey.hvideo.pojo.dto.RecordDto;
import com.harvey.hvideo.pojo.entity.SessionRecord;

import java.util.List;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 14:46
 */
public interface SessionRecordService extends IService<SessionRecord> {
    String PERSON_CONTENT_KEY = "video:chat:person:content:";
    String USER_INBOX_KEY = "video:chat:user:inbox:";
    String GROUP_MEMBER_KEY = "video:chat:group:members";
    String GROUP_CONTENT_KEY = "video:chat:group:content";
    long TIME_INTERVAL = 60 * 60 * 1000; // 一小时
    List<RecordDto> searchSessionRecord(long id);

    Long searchSessionRecordId(long user1, long user2);

    Long createSessionIfNeeded(long from, long to);

    List<MessageDto> viewRecord(long  to);

}
