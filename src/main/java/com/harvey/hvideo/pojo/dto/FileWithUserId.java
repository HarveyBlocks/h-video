package com.harvey.hvideo.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.Serializable;

/**
 * 用于异步存储视频文件,因为新线程不能获取当前请求的用户id.实在是取不出名字来了
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-04 13:40
 */
@Data
@AllArgsConstructor
public class FileWithUserId implements Serializable {
    private MultipartFile file;
    private File target;
    private long userId;
}
