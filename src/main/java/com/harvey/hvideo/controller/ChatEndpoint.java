package com.harvey.hvideo.controller;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 21:39
 */

import com.harvey.hvideo.pojo.dto.ChatCommand;
import com.harvey.hvideo.pojo.dto.GroupCommand;
import com.harvey.hvideo.service.ChatService;
import io.swagger.annotations.Api;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket在线聊天室
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2023-12-30 13:37
 */
@ServerEndpoint(value = "/chat")
@Component
@Api(tags = "聊天")
public class ChatEndpoint {

    @Resource
    private ChatService chatService;

    /**
     * 建立Websocket连接之后被调用<br>
     * 1. 将Session进行保存<br>
     * 2. 广播已登录用户消息<br>
     *
     * @param session websocket的会话
     * @param config  config
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        chatService.onOpen(session);
    }


    /**
     * 接收到客户端发送的数据时被调用<br>
     * 1. 张三向李四发消息的时候<br>
     * 2. 找到李四的Endpoint对象<br>
     * 3. 使用李四Endpoint里面的方法<br>
     * 4. 给李四发消息<br>
     *
     * @param chatCommand, 命令
     */
    @OnMessage
    public void onChatMessage(
            @RequestBody boolean isGroup,
            @RequestBody ChatCommand chatCommand,
            @RequestBody GroupCommand groupCommand) {
        if (isGroup) {
            chatService.onMessage(groupCommand);
        } else {
            chatService.onMessage(chatCommand);//传输消息
        }
    }


    /**
     * 连接关闭时被调用<br>
     * 1. 从在线好友列表中剔除<br>
     * 2. 同时广播消息, 提醒其他用户该人已下线<br>
     *
     * @param session websocket的session
     */
    @OnClose
    public void onClose(Session session) {
        chatService.onClose(session);
    }
}