package com.harvey.hvideo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.harvey.hvideo.pojo.dto.VideoDto;
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

    void clickVideo(Long videoId);

    List<VideoDto> queryHotVideo(Integer current);


    List<VideoDto> queryMyVideo(Integer current);

    static String clickedSetKey(Long videoId) {
        return RedisConstants.VIDEO_CLICKED_KEY + videoId;
    }

    void sendVideoToFans(Long videoId);

    ScrollResult<VideoDto> queryFollowVideos(Long lastTimestamp, Integer offset);

    List<VideoDto> queryVideoByTittle(Integer current, String tittle);

    boolean save2Es(Video video, String videoId);

    void saveSearchHistory(String tittle);

    boolean saveVideo(Video video);
}
