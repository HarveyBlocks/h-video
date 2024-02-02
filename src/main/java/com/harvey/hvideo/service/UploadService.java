package com.harvey.hvideo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 上传文件的业务,包括图片和视频
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-02 09:52
 */
public interface UploadService {
    void deleteFile(String constDir, String filename);


    String saveFile(String constDir, MultipartFile file) throws IOException;
}
