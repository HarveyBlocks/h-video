package com.harvey.hvideo.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-12 14:41
 */
@Data
public class GroupCommand implements Serializable {
    private StandardCommand mainCommand;
    private List<Long> targets; // 是组就是groupid(第一个),是人就是userid(第一个), 要不就是memberIds
    private String content; // 创建群聊就是群聊名称, 然后系统分配一个雪花ID
    private byte[] image;
    public enum StandardCommand{
        QUIT,JOIN,CREATE,CHAT
    }

}
