package com.harvey.hvideo.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.hvideo.dao.SessionRecordMapper;
import com.harvey.hvideo.pojo.dto.MessageDto;
import com.harvey.hvideo.pojo.dto.RecordDto;
import com.harvey.hvideo.pojo.dto.UserDto;
import com.harvey.hvideo.pojo.entity.Message;
import com.harvey.hvideo.pojo.entity.SessionRecord;
import com.harvey.hvideo.pojo.entity.User;
import com.harvey.hvideo.service.ChatService;
import com.harvey.hvideo.service.SessionRecordService;
import com.harvey.hvideo.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 14:46
 */
@Service
public class SessionRecordServiceImpl extends ServiceImpl<SessionRecordMapper, SessionRecord>
        implements SessionRecordService {


    @Resource
    private UserService userService;

    @Override
    public List<RecordDto> searchSessionRecord(long id) {
        List<SessionRecord> records = this.lambdaQuery()
                .eq(SessionRecord::getUser1, id)
                .or()
                .eq(SessionRecord::getUser2, id)
                .list();//还有.one(),.page等
        List<Long> userIds = records.stream().map(SessionRecord::getUser2).collect(Collectors.toList());
        List<User> users = userService.lambdaQuery().in(User::getId, userIds).list();
        List<RecordDto> result = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            SessionRecord record = records.get(i);
            User user = users.get(i);
            result.add(new RecordDto(record.getId(), user));
        }
        return result;
    }

    @Override
    public Long searchSessionRecordId(long user1, long user2) {
        SessionRecord one = this.lambdaQuery()
                .eq(SessionRecord::getUser1, user1)
                .eq(SessionRecord::getUser2, user2)
                .or()
                .eq(SessionRecord::getUser2, user1)
                .eq(SessionRecord::getUser1, user2)
                .one();
        return one==null?null:one.getId();

    }

    @Override
    public Long createSessionIfNeeded(long from, long to) {
        Long id = this.searchSessionRecordId(from, to);
        // 没有记录, 加上记录
        if (id == null) {
            SessionRecord record1 = new SessionRecord(from, to);
            id = (long) this.baseMapper.insert(record1);
        }
        return id;
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ChatService chatService;

    /**
     * 从Redis中查询聊天记录
     */
    @Override
    public List<MessageDto> viewRecord(long id) {
        long end = System.currentTimeMillis();
        Set<String> contentIdStrings = stringRedisTemplate.opsForZSet().range(PERSON_CONTENT_KEY + id, end - TIME_INTERVAL, end);
        if (contentIdStrings == null||contentIdStrings.isEmpty()) {
            // 没有这个聊天记录
            return Collections.emptyList();
        }
        List<Long> contentIds = contentIdStrings.stream().map(Long::parseLong).collect(Collectors.toList());
        List<Message> messages = chatService.lambdaQuery().in(Message::getId, contentIds).list();
        if (messages==null||messages.isEmpty()){
            return Collections.emptyList();
        }
        return message2ContentMessage(messages);
    }

    private ArrayList<MessageDto> message2ContentMessage(List<Message> messages) {
        ArrayList<MessageDto> contentMessages = new ArrayList<>();
        for (Message message : messages) {
            User user = userService.getById(message.getFromId());
            if (user==null){
                continue;
            }
            contentMessages.add(new MessageDto(new UserDto(user),message.getContent()));
        }
        return contentMessages;
    }



}
