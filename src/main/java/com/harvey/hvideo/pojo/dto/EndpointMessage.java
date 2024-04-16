package com.harvey.hvideo.pojo.dto;

import lombok.Data;

/**
 * TODO
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
