package com.harvey.hvideo.pojo.dto;

import com.harvey.hvideo.pojo.entity.Group;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-29 18:58
 */
@Data
@NoArgsConstructor
public class MessageDto implements Serializable {
    private UserDto fromUser;
    private Group group;
    private String content;
    private byte[] image;

    public MessageDto(UserDto fromUser, byte[] image) {
        this.fromUser = fromUser;
        this.image = image;
    }

    public MessageDto(UserDto fromUser, String content) {
        this.fromUser = fromUser;
        this.content = content;
    }

    public MessageDto(UserDto fromUser, Group fromGroup, byte[] image) {
        this.fromUser = fromUser;
        this.image = image;
        this.group = fromGroup;
    }

    public MessageDto(UserDto fromUser, Group fromGroup , String content) {
        this.fromUser = fromUser;
        this.content = content;
        this.group = fromGroup;
    }
}