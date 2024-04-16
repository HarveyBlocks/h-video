package com.harvey.hvideo.controller;

import com.alibaba.fastjson.JSON;
import com.harvey.hvideo.config.GetHttpSessionConfig;
import com.harvey.hvideo.pojo.dto.ChatCommand;
import com.harvey.hvideo.pojo.dto.EndpointMessage;
import com.harvey.hvideo.pojo.dto.GroupCommand;
import com.harvey.hvideo.service.ChatService;
import com.harvey.hvideo.service.GroupService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * WebSocket在线聊天室
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2023-12-30 13:37
 */
@Component
@Api(tags = "聊天")
@Slf4j
@ServerEndpoint(value = "/chat",configurator = GetHttpSessionConfig.class)
public class ChatEndpoint {


    private static ChatService chatService;

    private static GroupService groupService;

    @Autowired
    public void setChatService(ChatService chatService) {
        ChatEndpoint.chatService = chatService;
    }

    @Autowired
    public void setGroupService(GroupService groupService) {
        ChatEndpoint.groupService = groupService;
    }

    /**
     * 建立Websocket连接之后被调用<br>
     * 1. 将Session进行保存<br>
     * 2. 广播已登录用户消息<br>
     *
     * @param session websocket的会话
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        chatService.onOpen(session);
    }


    /**
     * 接收到客户端发送的数据时被调用<br>
     * 1. 张三向李四发消息的时候<br>
     * 2. 找到李四的Endpoint对象<br>
     * 3. 使用李四Endpoint里面的方法<br>
     * 4. 给李四发消息<br>
     */
    @OnMessage
    public void onChatMessage(@RequestParam("message") String message) {
        EndpointMessage endpointMessage = JSON.parseObject(message, EndpointMessage.class);
        boolean group = endpointMessage.isGroup();
        ChatCommand chatCommand = endpointMessage.getChatCommand();
        GroupCommand groupCommand = endpointMessage.getGroupCommand();
        if (group) {
            groupService.onMessage(groupCommand);
        } else {
            chatService.onMessage(chatCommand);//传输消息
        }
    }


    /**
     * 连接关闭时被调用<br>
     * 1. 从在线好友列表中剔除<br>
     * 2. 同时广播消息, 提醒其他用户该人已下线<br>
     */
    @OnClose
    public void onClose(CloseReason closeReason) {
        chatService.onClose();
    }

    // 连接异常
    @OnError
    public void onError(Throwable throwable) throws IOException {
        log.info("[websocket] 连接异常：，throwable", throwable);
    }
}