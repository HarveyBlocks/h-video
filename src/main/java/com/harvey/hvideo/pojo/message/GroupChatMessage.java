package com.harvey.hvideo.pojo.message;

import lombok.Data;
import lombok.ToString;


/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-29 18:58
 */
@Data
@ToString(callSuper = true)
public class GroupChatMessage {
    private Long from;
    private String content;
    private byte[] image;
    public GroupChatMessage(Long from, Long groupId, String content) {
        this.from = from;
        this.content = content;
    }
    public GroupChatMessage(Long from, Long groupId, byte[] image) {
        this.from = from;
        this.image = image;
    }
}
