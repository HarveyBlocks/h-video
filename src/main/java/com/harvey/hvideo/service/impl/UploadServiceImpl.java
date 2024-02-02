package com.harvey.hvideo.service.impl;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.harvey.hvideo.exception.BadRequestException;
import com.harvey.hvideo.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-02 09:52
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {


    @Override
    public  void deleteFile(final String constDir, String filename) {
        File file = new File(constDir, filename);
        if (file.isDirectory()) {
            throw new BadRequestException("错误的文件名称");
        }
        FileUtil.del(file);
    }


    private static String createNewFileName(final String constDir, String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // 判断目录是否存在
        File dir = new File(constDir, StrUtil.format("/{}/{}", d1, d2));
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.error("文件夹生成失败");
            }
        }
        // 生成文件名
        return StrUtil.format("/{}/{}/{}.{}", d1, d2, name, suffix);
    }

    @Override
    public String saveFile(final String constDir, MultipartFile file) throws IOException {
        // 获取原始文件名称
        String originalFilename = file.getOriginalFilename();
        // 生成新文件名
        String fileName = createNewFileName(constDir,originalFilename);
        // RE
        file.transferTo(new File(constDir, fileName));
        // 返回结果
        log.debug("文件上传成功，{}", fileName);
        return fileName;
    }

}
