package com.harvey.hvideo.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.hvideo.dao.GroupMapper;
import com.harvey.hvideo.pojo.dto.GroupCommand;
import com.harvey.hvideo.pojo.dto.MessageDto;
import com.harvey.hvideo.pojo.dto.UserDto;
import com.harvey.hvideo.pojo.entity.Group;
import com.harvey.hvideo.pojo.entity.Message;
import com.harvey.hvideo.pojo.entity.User;
import com.harvey.hvideo.pojo.vo.Result;
import com.harvey.hvideo.service.ChatService;
import com.harvey.hvideo.service.GroupService;
import com.harvey.hvideo.service.SessionRecordService;
import com.harvey.hvideo.service.UserService;
import com.harvey.hvideo.util.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-12 15:02
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {
    @Resource
    private ChatService chatService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(GroupCommand groupCommand) {
        // StandardChat from           target
        // Chat         当前用户       getTarget    群发content, 是文字就存Redis, 图片忽略
        // Create       当前用户       MembersIds    content: 群聊名, 群发这些人, 您被xx拉入群聊, 群聊的ID, RedisHash,Key:ID=群聊名,群成员, 数据库, 群聊ID, 群聊名
        // JOIN,        当前用户        groupId     群发: 新成员加入
        // QUIT         当前用户        groupId
        // 对当前所有
        if (groupCommand == null) {
            return;
        }
        List<Long> targets = groupCommand.getTargets();
        if (targets == null || targets.isEmpty()) {
            return;
        }
        Long target = null;
        GroupCommand.StandardCommand mainCommand = groupCommand.getMainCommand();
        if (targets.size() > 1 && mainCommand != GroupCommand.StandardCommand.CREATE) {
            return;
        }
        if (mainCommand != GroupCommand.StandardCommand.CREATE) {
            target = targets.get(0);
        }
        String content = groupCommand.getContent();
        switch (mainCommand) {
            case CREATE:
                targets.add(UserHolder.currentUserId());
                this.create(content, targets);
                break;
            case QUIT:
                this.quit(UserHolder.currentUserId(), target);
                break;
            case JOIN:
                this.join(UserHolder.currentUserId(), target);
                break;
            case CHAT:
                if (content != null) {
                    this.chat(UserHolder.currentUserId(), target, content);
                } else if (groupCommand.getImage() != null) {
                    log.warn("建议先用图片上传接口传输图片, 然后获取到图片地址, 然后吧图片地址作为Content过来, 走content的if分支");
                    this.chat(UserHolder.currentUserId(), target, groupCommand.getImage());
                } else {
                    log.error("啥都没有你传个啥子嘞?");
                }
        }
    }

    @Resource
    private UserService userService;

    private void create(String name, List<Long> members) {
        if (name == null || members == null || members.size() <= 2) {
            return;
        }
        // 存入数据库
        int groupId = this.baseMapper.insert(new Group(name));
        // 用户信息
        List<User> users = userService.listByIds(members);
        String[] existUserIdstring = users.stream().map(user -> user.getId().toString()).toArray(String[]::new);
        Set<Long> existUserIds = users.stream().map(User::getId).collect(Collectors.toSet());
        // 存入Redis
        stringRedisTemplate.opsForSet().add(SessionRecordService.GROUP_MEMBER_KEY + groupId, existUserIdstring);
        // 创建返回消息
        StringBuilder builder = new StringBuilder();
        User nowUser = userService.getById(UserHolder.currentUserId());
        builder.append("您已被\"").append(nowUser.getNickName()).append("\"拉入群聊: \"").append(name)
                .append("\", 成员包括: ");
        users.forEach(user -> builder.append(".\"").append(user.getNickName()).append("\", "));
        String json = JSON.toJSONString(new Result<>(builder.toString(), "拉入群聊"));
        // 广播群成员
        chatService.broadcastUsers(json, existUserIds);
    }


    private static final ExecutorService SINGLE = Executors.newSingleThreadExecutor();

    private void quit(Long userId, Long groupId) {
        String membersKey = SessionRecordService.GROUP_MEMBER_KEY + groupId;
        String userIdString = userId.toString();
        Boolean isMember = stringRedisTemplate.opsForSet()
                .isMember(membersKey, userIdString);
        if (Boolean.FALSE.equals(isMember)) {
            log.warn("请求删除群聊成员的不在群中");
            return;
        }
        // 从Redis中删除成员
        stringRedisTemplate.opsForSet().remove(membersKey, userIdString);

        Set<String> members = stringRedisTemplate.opsForSet().members(membersKey);
        if (members == null) {
            return;
        }

        // 发送信息
        StringBuilder builder = new StringBuilder();
        builder.append("成员: \"").append(userService.getById(userId).getNickName()).append("\"退出该群聊.");
        Set<Long> existMembersIds = members.stream().map(Long::parseLong).collect(Collectors.toSet());
        String quitJson = JSON.toJSONString(new Result<>(builder.toString(), "退出群聊"));
        builder.delete(0, builder.length());
        chatService.broadcastUsers(quitJson, existMembersIds);

        // 判断是否需要被删除
        if (members.size() > 2) {
            return;
        }
        // 删除群聊
        String groupName = this.getById(groupId).getName();
        SINGLE.execute(() -> {
            // 删除数据库信息
            this.baseMapper.deleteById(groupId);
            // 删除Redis信息
            stringRedisTemplate.delete(membersKey);
            // 删除该群的Redis聊天记录
            String chatKey = SessionRecordService.GROUP_CONTENT_KEY + groupId;
            Set<String> removeIds = stringRedisTemplate.opsForZSet().range(chatKey, 0L, System.currentTimeMillis());
            stringRedisTemplate.delete(chatKey);
            if (removeIds != null && !removeIds.isEmpty()) {
                // 数据库里的聊天记录删不删呢?
                chatService.removeByIds(removeIds);
            }
        });
        builder.append("群聊: \"").append(groupName).append("\" 人数过少, 已被删除.");
        String deleteGroupJson = JSON.toJSONString(new Result<>(builder.toString(), "删除群聊"));
        chatService.broadcastUsers(deleteGroupJson, existMembersIds);
    }


    private void join(Long userId, Long groupId) {
        String membersKey = SessionRecordService.GROUP_MEMBER_KEY + groupId;
        if (userId == null || groupId == null) {
            return;
        }
        // 获取用户信息
        User user = userService.getById(userId);
        Group group = this.getById(groupId);
        if (user == null || group == null) {
            return;
        }
        // 在Redis中增加成员
        stringRedisTemplate.opsForSet().add(membersKey, userId.toString());
        // 广播
        String message = "群聊: \"" + group.getName() +
                "\" 新增成员: \"" + user.getNickName() + "\", 大家欢迎!";
        String deleteGroupJson = JSON.toJSONString(new Result<>(message, "删除群聊"));
        chatService.broadcastUsers(deleteGroupJson, membersFromRedis(groupId));
    }


    private void chat(Long from, Long groupId, String content) {
        chatService.filter(content);
        // 封装Result , 存到Redis, 存到MySQL, 发送
        User user = userService.getById(from);
        if (user == null) {
            log.error("当前用户不存在???");
            return;
        }
        Group group = this.getById(groupId);
        if (group == null) {
            Result<?>  result = new Result<>(404, "当前群聊不存在");
            String resultJson = JSON.toJSONString(result);
            chatService.broadcastUsers(resultJson, List.of(from));
            return;
        }
        UserDto userDto = new UserDto(user);
        Result<MessageDto> result = new Result<>(new MessageDto(userDto, group, content), "群聊文字");
        String resultJson = JSON.toJSONString(result);
        SINGLE.execute(() -> {
            // 存到MySQL
            int contentId = chatService.insert(new Message(from, content, null, groupId));
            // 存到Redis, key是群聊ID,  value是contentID, score是当前时间
            String key = SessionRecordService.GROUP_CONTENT_KEY + groupId;
            stringRedisTemplate.opsForZSet().add(key, String.valueOf(contentId), System.currentTimeMillis());
        });
        chatService.broadcastUsers(resultJson, membersFromRedis(groupId));
    }


    private void chat(Long from, Long groupId, byte[] image) {
        User user = userService.getById(from);
        if (user == null) {
            log.error("当前用户不存在???");
            return;
        }
        Group group = this.getById(groupId);
        if (group == null) {
            Result<?> result = new Result<>(404, "当前群聊不存在");
            String resultJson = JSON.toJSONString(result);
            chatService.broadcastUsers(resultJson, List.of(from));
            return;
        }
        UserDto userDto = new UserDto(user);
        Result<?> result = new Result<>(new MessageDto(userDto, group, image), "群聊图片");

        String resultJson = JSON.toJSONString(result);
        chatService.broadcastUsers(resultJson, membersFromRedis(groupId));
    }

    @Override
    public Set<Long> membersFromRedis(Long groupId) {
        String membersKey = SessionRecordService.GROUP_MEMBER_KEY + groupId;
        Set<String> membersStr = stringRedisTemplate.opsForSet().members(membersKey);
        if (membersStr == null || membersStr.isEmpty()) {
            return Collections.emptySet();
        }
        return membersStr.stream().map(Long::parseLong).collect(Collectors.toSet());
    }


}
