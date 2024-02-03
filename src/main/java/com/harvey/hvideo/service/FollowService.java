package com.harvey.hvideo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.harvey.hvideo.pojo.dto.UserDTO;
import com.harvey.hvideo.pojo.entity.Follow;
import com.harvey.hvideo.util.RedisConstants;

import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
public interface FollowService extends IService<Follow> {

    static String followedKey(Long authorId){
        return RedisConstants.FOLLOWED_KEY+authorId;
    }
    static String followedInboxKey(Long fanId){
        return RedisConstants.FOLLOWED_INBOX_KEY + fanId;
    }


    void followOrCancel(Long authorId, boolean canFollow);

    boolean isFollowed(Long authorId);

    List<UserDTO> followInteraction(Long user1Id, Long user2Id);

    List<UserDTO> friendList(Long userId);

    List<UserDTO> followList(Long id);

    List<UserDTO> queryFanList(Long authorId, Integer current);
}
