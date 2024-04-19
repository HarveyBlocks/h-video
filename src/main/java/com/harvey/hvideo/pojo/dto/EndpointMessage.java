package com.harvey.hvideo.pojo.dto;

import lombok.Data;

/**
 * Endpoint 的 传参的参数 ,json转缓存EndpointMessage
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-16 22:00
 */
@Data
public class EndpointMessage {
    private boolean group;
    private ChatCommand chatCommand;
    private GroupCommand groupCommand;
}
