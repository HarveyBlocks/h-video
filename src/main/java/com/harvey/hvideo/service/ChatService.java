package com.harvey.hvideo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.harvey.hvideo.pojo.dto.ChatCommand;
import com.harvey.hvideo.pojo.dto.UserDto;
import com.harvey.hvideo.pojo.entity.Message;

import javax.websocket.Session;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 22:35
 */
public interface ChatService extends IService<Message> {
    // ConcurrentHashMap, 线程安全的Map集合


    void broadcastUsers(String json, Collection<Long> userIds);

    void onMessage(ChatCommand chatCommand, UserDto userDto);

    void filter(String content);

    void onOpen(Session session, UserDto user);

    void onClose(UserDto userDto);


    int insert(Message message);
}
