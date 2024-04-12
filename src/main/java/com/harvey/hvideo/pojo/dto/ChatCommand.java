package com.harvey.hvideo.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 23:43
 */
@Data
public class ChatCommand implements Serializable {
    private Long target;
    private String content;
    private byte[] image;
}
