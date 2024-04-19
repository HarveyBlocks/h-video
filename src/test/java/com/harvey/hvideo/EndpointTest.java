package com.harvey.hvideo;

import com.harvey.hvideo.pojo.dto.UserDto;
import com.harvey.hvideo.service.ChatService;
import com.harvey.hvideo.util.UserHolder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 想要测试websocket但无从下手
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-16 19:19
 */
@SpringBootTest
public class EndpointTest {
    @Resource
    public ChatService chatService;
    @Test
    void testEndpoint(){

    }
}
