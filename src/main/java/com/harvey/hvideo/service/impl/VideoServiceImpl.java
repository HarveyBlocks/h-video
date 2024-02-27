package com.harvey.hvideo.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.harvey.hvideo.Constants;
import com.harvey.hvideo.dao.VideoMapper;
import com.harvey.hvideo.pojo.dto.UserDTO;
import com.harvey.hvideo.pojo.dto.VideoDTO;
import com.harvey.hvideo.pojo.dto.VideoDoc;
import com.harvey.hvideo.pojo.entity.Follow;
import com.harvey.hvideo.pojo.entity.User;
import com.harvey.hvideo.pojo.entity.Video;
import com.harvey.hvideo.pojo.vo.ScrollResult;
import com.harvey.hvideo.service.FollowService;
import com.harvey.hvideo.service.UserService;
import com.harvey.hvideo.service.VideoService;
import com.harvey.hvideo.util.RedisConstants;
import com.harvey.hvideo.util.UserHolder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
            this.save2Es(video, videoId.toString());
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
        boolean updateSuccess = proxy.update().setSql("click = click + 1").eq("id", videoId).update();
        if (updateSuccess) {
            stringRedisTemplate.opsForSet().add(clickedSetKey, userId);
        }
    }

    private Boolean checkIsMember(String setKey, String userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(setKey, userId));
    }

    /**
     * 每十分钟删除视频点击记录
     */
    @PostConstruct
    public void delClickedHistory() {
        while (true) {
            Set<String> keys = null;
            try {
                keys = stringRedisTemplate.keys(RedisConstants.VIDEO_CLICKED_KEY + "*");
            } catch (NullPointerException e) {
                log.debug("没有key可以清空:" + e.getMessage());
            }
            if (keys == null || keys.isEmpty()) {
                return;
            }
            stringRedisTemplate.delete(keys);
            log.debug("完成一次清空观看记录");
            try {
                // 无奈之举......Netty也能做定时任务.....
                Thread.sleep(Constants.CLEAR_CLICK_HISTORY_WAIT_SECONDS * 1000);
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
        return records.stream().map(VideoDTO::new).collect(Collectors.toList());
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<VideoDTO> queryMyVideo(Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Video> page = this.query().select("id", "title", "kicked", "images", "comments").eq("user_id", user.getId()).page(new Page<>(current, Constants.MAX_PAGE_SIZE));
        // 获取当前页数据
        return page.getRecords().stream().map(VideoDTO::new).collect(Collectors.toList());
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
        List<Follow> follows = followService.query().select("fan_id").eq("author_id", UserHolder.currentUserId().toString()).list();
        for (Follow follow : follows) {
            stringRedisTemplate.opsForZSet().add(FollowService.followedInboxKey(follow.getFanId()), String.valueOf(videoId), System.currentTimeMillis());
        }
    }

    private Set<ZSetOperations.TypedTuple<String>> getVideoIdsWithTimestamp(Long lastTimestamp, Integer offset) {
        return stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(FollowService.followedInboxKey(UserHolder.currentUserId()), 0, lastTimestamp, offset, Constants.DEFAULT_PAGE_SIZE);
    }

    @Override
    public ScrollResult<VideoDTO> queryFollowVideos(Long lastTimestamp, Integer offset) {
        Set<ZSetOperations.TypedTuple<String>> typedTuples = getVideoIdsWithTimestamp(lastTimestamp, offset);
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
        return new ScrollResult<>(videos.stream().map(VideoDTO::new)
                .collect(Collectors.toList()), minTime, newOffset);
    }


    /**
     * ES 依据tittle查询
     *
     * @param current 当前页码
     */
    @Override
    public List<VideoDTO> queryVideoByTittle(Integer current, String tittle) {
        // 1. 创建SearchRequest请求,GET /索引库/_search
        SearchRequest request = new SearchRequest(VIDEO_INDEX);

        SearchSourceBuilder s = new SearchSourceBuilder();
        // 2. 组织DSL语句
        MatchQueryBuilder matchQuery = new MatchQueryBuilder("all",tittle);
        s.query(matchQuery);
        s.sort("click", SortOrder.DESC);
        s.from((current - 1) * Constants.MAX_PAGE_SIZE);
        s.size(Constants.MAX_PAGE_SIZE);
        request.source(s);
        // 3. 发送请求
        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("查询失败", e);
            throw new RuntimeException("查询失败");
        }

        // 4、解析响应
        SearchHits searchHits = response.getHits();

        // 5. 查询总条数
        log.debug("总共查询" + searchHits.getTotalHits().value + "条记录");//201

        // 6, 获取结果数组
        SearchHit[] hits = searchHits.getHits();
        // 7. 遍历,转化
        return Arrays.stream(hits).map(
                // 解析Json字符
                hit -> new VideoDTO(new Video(JSON.parseObject(
                        // 获取数据信息
                        hit.getSourceAsString(), VideoDoc.class)
                ))).collect(Collectors.toList());
    }


    @Resource
    private RestHighLevelClient restClient;
    public static final String VIDEO_INDEX = "h_video_video";


    @Override
    public boolean save2Es(Video video, String videoId) {
        try {
            // 1. 准备Request 对象
            // 代替 "POST /索引库名/_doc/文档id"
            IndexRequest request = new IndexRequest(VIDEO_INDEX).id(videoId);
            // 2. 准备Json文档
            String addSource = JSON.toJSONString(new VideoDoc(video));
            System.out.println(addSource);
            request.source(addSource, XContentType.JSON);
            // 3. 发送请求
            IndexResponse index = restClient.index(request, RequestOptions.DEFAULT);
            int status = index.status().getStatus();
            return status / 100 == 2;
        } catch (IOException e) {
            log.error("es保存失败", e);
            throw new RuntimeException("es保存失败");
        }

    }

    @Override
    public void saveSearchHistory(String tittle) {
        // 保存查询记录
        String searchHistoryKey = RedisConstants.SEARCH_HISTORY + UserHolder.currentUserId();
        stringRedisTemplate.opsForSet().add(searchHistoryKey, tittle);
    }

    @Override
    public boolean saveVideo(Video video) {
        boolean saved2Db = this.save(video);
        LambdaQueryWrapper<Video> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select().eq(Video::getVideoPath, video.getVideoPath());
        Video video1 = baseMapper.selectOne(lambdaQueryWrapper);
        addAuthor(video1);
        if (saved2Db) {
            return this.save2Es(video1, video1.getId().toString());
        } else {
            return false;
        }
    }


    private List<Video> queryCompleteVideos(List<String> videoIds) {
        String videoIdsStr = String.join(",", videoIds);
        List<Video> videos = this.query().in("id", videoIds).last("order by field(id," + videoIdsStr + ")").list();
        // 让video信息完整
        videos.forEach((this::addAuthor));
        return videos;
    }


    private void addAuthor(Video video) {
        Long userId = video.getUserId();
        User user = userService.getById(userId);
        video.setNickName(user.getNickName());
        video.setIcon(user.getIcon());
    }
}
