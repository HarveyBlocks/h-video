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

    List<RecordDto> searchSessionRecord();

    Long searchSessionRecordId(long user1, long user2);

    Long createSessionIfNeeded(long from, long to);

    List<MessageDto> viewRecord(long  to);

}
