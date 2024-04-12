package com.harvey.hvideo.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.hvideo.dao.MessageMapper;
import com.harvey.hvideo.pojo.dto.ChatCommand;
import com.harvey.hvideo.pojo.dto.GroupCommand;
import com.harvey.hvideo.pojo.dto.MessageDto;
import com.harvey.hvideo.pojo.dto.UserDto;
import com.harvey.hvideo.pojo.entity.Message;
import com.harvey.hvideo.pojo.entity.User;
import com.harvey.hvideo.pojo.vo.Result;
import com.harvey.hvideo.properties.ConstantsProperties;
import com.harvey.hvideo.service.ChatService;
import com.harvey.hvideo.service.GroupService;
import com.harvey.hvideo.service.SessionRecordService;
import com.harvey.hvideo.service.UserService;
import com.harvey.hvideo.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 22:36
 */
@Slf4j
@Service
@EnableConfigurationProperties(ConstantsProperties.class)
public class ChatServiceImpl extends ServiceImpl<MessageMapper, Message> implements ChatService {
    @Resource
    private UserService userService;

    /**
     * 向特定用户发送消息
     *
     * @param json json数据
     */
    private void send2User(String json, Long userId) {
        Session session = ONLINE_USERS.get(userId);
        if (session == null) {
            // 用户已下线
            return;
        }
        try {
            session.getBasicRemote()// 发送同步消息
                    .sendText(json);
        } catch (IOException e) {
            log.error("发送`{}`失败", session.getId());
        }
    }


    /**
     * 广播全体用户
     *
     * @param json json数据
     */
    private void broadcastAllUser(String json) {
        // 遍历map集合
        broadcastUsers(json, ONLINE_USERS.keySet());
    }

    @Override
    public void broadcastUsers(String json, Collection<Long> userIds) {
        // ONLINE_USERS变成了entries单列集合
        for (Long userId : userIds) {
            this.send2User(json, userId);
        }
    }

    @Override
    public void onMessage(ChatCommand chatCommand) {
        if (chatCommand == null) {
            return;
        }
        // 命令           From        Target
        // Chat         当前用户       getTarget    发送content 是文字就存Redis
        if (chatCommand.getContent() != null) {
            this.chat(UserHolder.currentUserId(), chatCommand.getTarget(), chatCommand.getContent());
        } else if (chatCommand.getImage() != null) {
            log.warn("建议先用图片上传接口传输图片, 然后获取到图片地址, 然后吧图片地址作为Content过来, 走content的if分支");
            this.chat(UserHolder.currentUserId(), chatCommand.getTarget(), chatCommand.getImage());
        } else {
            log.error("啥都没有你发个啥 ? ");
        }

    }

    @Resource
    private SessionRecordService sessionService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final ExecutorService SINGLE = Executors.newSingleThreadExecutor();

    @Resource
    private ConstantsProperties constantsProperties;
    @Override
    public void filter(String content) {
        List<String> sensitiveWords = constantsProperties.getSensitiveWords();
        if(sensitiveWords==null||sensitiveWords.isEmpty()){
            return;
        }
        for (String sensitiveWord : sensitiveWords) {
            content = content.replace(sensitiveWord, "富强民主文明和谐自由平等公正法制爱国敬业诚信意识");
        }
    }


    private void chat(Long userId, Long target, String content) {
        filter(content);
        // 封装Result , 存到Redis, 存到MySQL, 发送
        User user = userService.getById(userId);
        if (user == null) {
            log.error("当前用户不存在数据库中");
            return;
        }
        UserDto from = new UserDto(user);
        Result<MessageDto> result = new Result<>(new MessageDto(from, content), "私聊文字");
        String resultJson = JSON.toJSONString(result);
        SINGLE.execute(() -> {

            Long sessionRecordId = sessionService.createSessionIfNeeded(userId, target);
            // 存到Redis, key是私聊ID, 一组私聊一个ID, value是contentID, score是当前时间

            // 存到MySQL
            ChatService thisService = (ChatService) AopContext.currentProxy();
            int contentId = thisService.insert(new Message(userId, content,sessionRecordId,null));
            // 内容
            String key = SessionRecordService.PERSON_CONTENT_KEY + sessionRecordId;
            stringRedisTemplate.opsForZSet().add(key, String.valueOf(contentId), System.currentTimeMillis());
        });
        send2User(resultJson, target);
    }


    private void chat(Long userId, Long target, byte[] image) {
        // 封装Result
        User user = userService.getById(userId);
        if (user == null) {
            log.error("当前用户不存在数据库中");
            return;
        }
        UserDto from = new UserDto(user);
        Result<MessageDto> result = new Result<>(new MessageDto(from, image), "私聊图片");
        String resultJson = JSON.toJSONString(result);
        send2User(resultJson, target);
    }

    @Override
    public void onOpen(Session session) {
        // 1.2 存入集合. 需要键username
        ChatService.ONLINE_USERS.put(UserHolder.currentUserId(), session);
        // 2. 广播消息
        // 需要将登录的所有用户的用户名推送给所有用户
        this.broadcastAllUser(JSON.toJSONString(new Result<>(userService.getById(UserHolder.currentUserId()), "用户已上线")));
    }

    @Override
    public void onClose(Session session) {
        // 1. 从在线好友列表中剔除当前用户的Session对象,
        try (Session ignored = ChatService.ONLINE_USERS.remove(UserHolder.currentUserId())) {
            // 2. 通知其他所有用户当前用户已下线
            this.broadcastAllUser(JSON.toJSONString(
                    new Result<>(userService.getById(UserHolder.currentUserId()), "用户已下线"))
            );
        } catch (IOException e) {
            log.error("session关闭错误", e);
        }
    }

    @Resource
    private GroupService groupService;

    @Override
    public void onMessage(GroupCommand groupCommand) {
        groupService.onMessage(groupCommand);
    }

    @Override
    @Transactional
    public int insert(Message message) {
        return this.baseMapper.insert(message);
    }


}
