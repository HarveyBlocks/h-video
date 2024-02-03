package com.harvey.hvideo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.harvey.hvideo.pojo.entity.Video;
import com.harvey.hvideo.pojo.vo.ScrollResult;
import com.harvey.hvideo.util.RedisConstants;

import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
public interface VideoService extends IService<Video> {

    Video viewVideo(Long videoId);

    List<Video> queryHotVideo(Integer current);

    void clickVideo(Long videoId);

    List<Video> queryMyVideo(Integer current);

    static String clickedSetKey(Long videoId) {
        return RedisConstants.VIDEO_CLICKED_KEY + videoId;
    }

    void sendVideoToFans(Long videoId);

    ScrollResult<Video> queryFollowVideos(Long lastTimestamp, Integer offset);
}
