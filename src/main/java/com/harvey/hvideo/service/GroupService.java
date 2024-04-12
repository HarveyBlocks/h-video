package com.harvey.hvideo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.harvey.hvideo.pojo.dto.GroupCommand;
import com.harvey.hvideo.pojo.entity.Group;

import java.util.Set;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-12 14:54
 */
public interface GroupService extends IService<Group>  {
    void onMessage(GroupCommand groupCommand);

    Set<Long> membersFromRedis(Long groupId);
}
