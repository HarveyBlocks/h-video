package com.harvey.hvideo.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.hvideo.dao.FollowMapper;
import com.harvey.hvideo.pojo.dto.UserDTO;
import com.harvey.hvideo.pojo.entity.Follow;
import com.harvey.hvideo.pojo.entity.Video;
import com.harvey.hvideo.service.FollowService;
import com.harvey.hvideo.service.UserService;
import com.harvey.hvideo.service.VideoService;
import com.harvey.hvideo.util.Constants;
import com.harvey.hvideo.util.TimeUtil;
import com.harvey.hvideo.util.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void followOrCancel(Long authorId, boolean canFollow) {
        if (authorId == null) {
            return;
        }
        if (canFollow) {
            follow(authorId);
        } else {
            // 删除关注关系
            cancel(authorId);
        }
    }

    @Resource
    private VideoService videoService;

    private void follow(Long authorId) {
        FollowService followService = (FollowService) AopContext.currentProxy();
        Long fanId = UserHolder.currentUserId();
        // 往关注里添加信息
        Follow follow = new Follow();
        follow.setAuthorId(authorId);
        follow.setFanId(fanId);

        if (followService.save(follow)) {
            stringRedisTemplate.opsForSet().add(FollowService.followedKey(fanId), authorId.toString());
            List<Video> videos = videoService.query().eq("user_id", authorId.toString()).list();
            if (videos.isEmpty()){
                return;
            }
            Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>(videos.size());
            videos.forEach(video -> tuples.add(new VideoIdTuple(video)));
            stringRedisTemplate.opsForZSet().add(FollowService.followedInboxKey(fanId), tuples);
        }
    }

    static class VideoIdTuple implements ZSetOperations.TypedTuple<String> {
        private final Video video;

        public VideoIdTuple(Video video) {
            this.video = video;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public String getValue() {
            return video.getId().toString();
        }

        @Override
        public Double getScore() {
            return TimeUtil.toMillion(video.getCreateTime()).doubleValue();
        }

        @Override
        public int compareTo(ZSetOperations.TypedTuple<String> o) {
            // 经源码确认, 这个在添加到Redis的逻辑中, 这个方法的实现是无关紧要的
            return 0;
        }
    }

    private void cancel(Long authorId) {
        FollowService followService = (FollowService) AopContext.currentProxy();
        Long fanId = UserHolder.currentUserId();
        boolean success = followService.remove(
                new LambdaQueryWrapper<Follow>().select()
                        .eq(Follow::getFanId, fanId)
                        .eq(Follow::getAuthorId, authorId)
        );

        if (success) {
            stringRedisTemplate.opsForSet().remove(FollowService.followedKey(fanId), authorId.toString());
            stringRedisTemplate.delete(FollowService.followedInboxKey(fanId));
        }
    }

    @Override
    public boolean isFollowed(Long authorId) {
        if (authorId == null) {
            return false;
        }
        Long fanId = UserHolder.currentUserId();
        Boolean followed = checkIsMember(FollowService.followedKey(fanId), authorId.toString());
        return Boolean.TRUE.equals(followed);
    }

    private Boolean checkIsMember(String followedSetKey, String fanId) {
        return stringRedisTemplate.opsForSet().isMember(followedSetKey, fanId);
    }

    @Resource
    private UserService userService;

    @Override
    public List<UserDTO> followInteraction(Long user1Id, Long user2Id) {
        log.debug(String.valueOf(user1Id));
        log.debug(String.valueOf(user2Id));
        Set<String> ids = stringRedisTemplate.opsForSet()
                .intersect(FollowService.followedKey(user1Id), FollowService.followedKey(user2Id));
        log.debug(String.valueOf(ids));
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userId2User(ids);
    }

    @Override
    public List<UserDTO> friendList(Long userId) {

        Set<String> ids = stringRedisTemplate.opsForSet().members(FollowService.followedKey(userId));
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> friendIds = new ArrayList<>();
        ids.forEach(
                (id) -> {
                    Boolean isFriend = stringRedisTemplate.opsForSet()
                            .isMember(FollowService.followedKey(Long.parseLong(id)), userId.toString());
                    if (Boolean.TRUE.equals(isFriend)) {
                        friendIds.add(id);
                    }
                }
        );
        return userId2User(friendIds);
    }

    @Override
    public List<UserDTO> followList(Long id) {
        Set<String> ids = stringRedisTemplate.opsForSet().members(FollowService.followedKey(id));
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userId2User(ids);
    }

    @Override
    public List<UserDTO> queryFanList(Long authorId, Integer current) {
        // 获取登录用户
        // 根据用户查询
        List<Follow> records = baseMapper.selectPage(
                new Page<>(current, Constants.MAX_PAGE_SIZE),
                new LambdaQueryWrapper<Follow>().select(Follow::getFanId).eq(Follow::getAuthorId, authorId)
        ).getRecords();
        // 获取当前页数据
        List<String> fanIds = records.stream()
                .map(f -> f.getFanId().toString()).collect(Collectors.toList());
        return userId2User(fanIds);
    }

    private List<UserDTO> userId2User(Collection<String> ids) {
        return userService.listByIds(ids).stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }
}
