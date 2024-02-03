package com.harvey.hvideo.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.hvideo.dao.VideoMapper;
import com.harvey.hvideo.pojo.dto.UserDTO;
import com.harvey.hvideo.pojo.entity.Follow;
import com.harvey.hvideo.pojo.entity.User;
import com.harvey.hvideo.pojo.entity.Video;
import com.harvey.hvideo.pojo.vo.ScrollResult;
import com.harvey.hvideo.service.FollowService;
import com.harvey.hvideo.service.UserService;
import com.harvey.hvideo.service.VideoService;
import com.harvey.hvideo.util.Constants;
import com.harvey.hvideo.util.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {
    @Resource
    private UserService userService;

    @Override
    public Video viewVideo(Long videoId) {
        // 查看video
        Video video = this.getById(videoId);
        // 一部视频需要作者信息
        addAuthor(video);
        return video;
    }


    @Override
    public List<Video> queryHotVideo(Integer current) {
        // 根据用户查询
        Page<Video> page = this.query()
                .orderByDesc("clicked")
                .page(new Page<>(current, Constants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Video> records = page.getRecords();
        // 查询用户
        records.forEach(this::addAuthor);
        return records;
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    @Transactional
    public void clickVideo(Long videoId) {
        String userId = UserHolder.currentUserId().toString();
        String clickedSetKey = VideoService.clickedSetKey(videoId);
        boolean clicked = checkIsMember(clickedSetKey, userId);
        VideoService videoService = (VideoService) AopContext.currentProxy();
        if (clicked) {
            // 不算点击量
            return;
        }
        // 增加点击量
        boolean updateSuccess = videoService.update()
                .setSql("clicked = clicked + 1").eq("id", videoId).update();
        if (updateSuccess) {
            stringRedisTemplate.opsForZSet().add(clickedSetKey, userId, System.currentTimeMillis());
        }

    }

    @Override
    public List<Video> queryMyVideo(Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Video> page = this.query().select("id", "title", "kicked", "images", "comments")
                .eq("user_id", user.getId())
                .page(new Page<>(current, Constants.MAX_PAGE_SIZE));
        // 获取当前页数据
        return page.getRecords();
    }

    @Resource
    private FollowService followService;


    /**
     * 将video推送给粉丝
     *
     * @param videoId videoId
     */
    @Override
    public void sendVideoToFans(Long videoId) {
        // 查询视频作者的所有粉丝
        //select `user_id` from `tb_follow` where `follow_user_id` = 2;
        List<Follow> follows = followService.query()
                .select("fan_id")
                .eq("author_id",UserHolder.currentUserId().toString()).list();
        for (Follow follow : follows) {
            stringRedisTemplate.opsForZSet().add(
                    FollowService.followedInboxKey(follow.getFanId()),
                    String.valueOf(videoId),
                    System.currentTimeMillis()
            );
        }
    }

    private Set<ZSetOperations.TypedTuple<String>> getVideoIdsWithTimestamp(Long lastTimestamp, Integer offset) {
        return stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(
                        FollowService.followedInboxKey(UserHolder.currentUserId()),
                        0, lastTimestamp,
                        offset, Constants.DEFAULT_PAGE_SIZE
                );
    }

    @Override
    public ScrollResult<Video> queryFollowVideos(Long lastTimestamp, Integer offset) {
        Set<ZSetOperations.TypedTuple<String>> typedTuples
                = getVideoIdsWithTimestamp(lastTimestamp, offset);
        if (typedTuples == null || typedTuples.isEmpty()) {
            log.error("没有");
            return new ScrollResult<>(null, lastTimestamp, offset);
        }

        int newOffset = 0;
        long minTime = lastTimestamp;

        int size = typedTuples.size();
        List<String> videoIds = new ArrayList<>(size);
        // 获得videoIds,offset
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            videoIds.add(typedTuple.getValue());
            // 记录offset
            Double score = typedTuple.getScore();
            if (score == null) {
                log.error("score==null:" + typedTuple.getValue());
                continue;//认为score为无穷大
            }
            if (score.longValue() < minTime) {
                minTime = score.longValue();
                newOffset = 0;
            }
            newOffset++;
        }

        // 查询完整视频
        List<Video> videos = queryCompleteVideos(videoIds);
        log.debug("newOffset=" + newOffset);
        log.debug("minTime=" + minTime);
        return new ScrollResult<>(videos, minTime, newOffset);
    }


    private List<Video> queryCompleteVideos(List<String> videoIds) {
        String videoIdsStr = String.join(",", videoIds);
        List<Video> videos = this.query().in("id", videoIds)
                .last("order by field(id," + videoIdsStr + ")").list();
        // 让video信息完整
        videos.forEach(this::addAuthor);
        return videos;
    }


    private Boolean checkIsMember(String setKey, String userId) {
        return stringRedisTemplate.opsForZSet().score(setKey, userId) != null;
    }


    private void addAuthor(Video video) {
        Long userId = video.getUserId();
        User user = userService.getById(userId);
        video.setNickName(user.getNickName());
        video.setIcon(user.getIcon());
    }
}
