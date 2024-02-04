package com.harvey.hvideo.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.hvideo.dao.VideoMapper;
import com.harvey.hvideo.pojo.dto.UserDTO;
import com.harvey.hvideo.pojo.dto.VideoDTO;
import com.harvey.hvideo.pojo.entity.Follow;
import com.harvey.hvideo.pojo.entity.User;
import com.harvey.hvideo.pojo.entity.Video;
import com.harvey.hvideo.pojo.vo.ScrollResult;
import com.harvey.hvideo.Constants;
import com.harvey.hvideo.properties.AuthProperties;
import com.harvey.hvideo.properties.ConstantsProperties;
import com.harvey.hvideo.service.FollowService;
import com.harvey.hvideo.service.UserService;
import com.harvey.hvideo.service.VideoService;
import com.harvey.hvideo.util.RedisConstants;
import com.harvey.hvideo.util.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (video != null) {
            VideoService proxy = (VideoService) AopContext.currentProxy();
            proxy.clickVideo(videoId);
            // 一部视频需要作者信息
            addAuthor(video);
        }
        return video;
    }

    @Transactional
    @Override
    public void clickVideo(Long videoId) {
        String userId = UserHolder.currentUserId().toString();
        String clickedSetKey = VideoService.clickedSetKey(videoId);
        boolean clicked = checkIsMember(clickedSetKey, userId);
        VideoService proxy = (VideoService) AopContext.currentProxy();
        if (clicked) {
            // 不算点击量
            return;
        }
        // 增加点击量
        boolean updateSuccess = proxy.update()
                .setSql("click = click + 1").eq("id", videoId).update();
        if (updateSuccess) {
            stringRedisTemplate.opsForSet().add(clickedSetKey, userId);
        }
    }

    private Boolean checkIsMember(String setKey, String userId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(setKey, userId));
    }

    /**
     * 每十分钟删除视频点击记录
     */
    @PostConstruct
    public void delClickedHistory() {
        while (true){
            Set<String> keys = null;
            try {
                keys = stringRedisTemplate
                        .keys(RedisConstants.VIDEO_CLICKED_KEY + "*");
            } catch (NullPointerException e) {
                log.debug("没有key可以清空:" + e.getMessage());
            }
            if (keys == null || keys.isEmpty()) {
                return;
            }
            stringRedisTemplate.delete(keys);
            log.debug("完成一次清空观看记录");
            try {
                Thread.sleep(Constants.CLEAR_CLICK_HISTORY_WAIT_SECONDS*1000);
            } catch (InterruptedException ignored) {
            }
        }
    }


    @Override
    public List<VideoDTO> queryHotVideo(Integer current) {
        // 根据用户查询
        Page<Video> page = this.query()
                .orderByDesc("click")
                .page(new Page<>(current, Constants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Video> records = page.getRecords();
        // 查询用户
        records.forEach(this::addAuthor);
        return records.stream()
                .map(VideoDTO::new).collect(Collectors.toList());
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<VideoDTO> queryMyVideo(Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Video> page = this.query().select("id", "title", "kicked", "images", "comments")
                .eq("user_id", user.getId())
                .page(new Page<>(current, Constants.MAX_PAGE_SIZE));
        // 获取当前页数据
        return page.getRecords().stream()
                .map(VideoDTO::new).collect(Collectors.toList());
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
                .eq("author_id", UserHolder.currentUserId().toString()).list();
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
    public ScrollResult<VideoDTO> queryFollowVideos(Long lastTimestamp, Integer offset) {
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
        return new ScrollResult<>(videos.stream()
                .map(VideoDTO::new).collect(Collectors.toList()), minTime, newOffset);
    }


    /**
     * TODO ES 依据tittle查询
     *
     * @param current 当前页码
     */
    @Override
    public List<VideoDTO> queryVideoByTittle(Integer current, String tittle) {
        return null;
    }

    @Override
    public void saveSearchHistory(String tittle) {
        // 保存查询记录
        String searchHistoryKey = RedisConstants.SEARCH_HISTORY + UserHolder.currentUserId();
        stringRedisTemplate.opsForSet().add(searchHistoryKey, tittle);
    }


    private List<Video> queryCompleteVideos(List<String> videoIds) {
        String videoIdsStr = String.join(",", videoIds);
        List<Video> videos = this.query().in("id", videoIds)
                .last("order by field(id," + videoIdsStr + ")").list();
        // 让video信息完整
        videos.forEach(this::addAuthor);
        return videos;
    }


    private void addAuthor(Video video) {
        Long userId = video.getUserId();
        User user = userService.getById(userId);
        video.setNickName(user.getNickName());
        video.setIcon(user.getIcon());
    }
}
