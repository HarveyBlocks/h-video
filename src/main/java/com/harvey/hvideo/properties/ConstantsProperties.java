package com.harvey.hvideo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-04 17:28
 */
@Data
@ConfigurationProperties(prefix = "h-video.constants")
public class ConstantsProperties {
    private String authorizationHeader;
    private String videoUploadDir;
    private String imageUploadDir;
    private String restrictRequestTimes;
    private String clearClickHistoryWaitSeconds;
    private String maxPageSize;
    private String defaultPageSize;
}
