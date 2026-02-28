package com.harvey.hvideo.listener;

import com.harvey.hvideo.util.IpTool;
import com.harvey.hvideo.util.TimeUtil;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听器
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-19 16:09
 */
@Component
public class HVideoListener {
    @EventListener(ContextClosedEvent.class)
    public void onClosedEvent(ContextClosedEvent event) {
        IpTool.close();
        System.out.println(TimeUtil.toTime(event.getTimestamp()) + ", Ip文件已关闭");
    }
}
