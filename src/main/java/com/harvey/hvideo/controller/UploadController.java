package com.harvey.hvideo.controller;

import com.harvey.hvideo.pojo.vo.Null;
import com.harvey.hvideo.pojo.vo.Result;
import com.harvey.hvideo.properties.ConstantsProperties;
import com.harvey.hvideo.service.UploadService;
import com.harvey.hvideo.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 上传图片和头像
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 21:13
 */
@Slf4j
@RestController
@Api(tags = "上传或删除文件")
@RequestMapping("upload")
@EnableConfigurationProperties(ConstantsProperties.class)
public class UploadController {
    @Resource
    private ConstantsProperties constantsProperties;
    @Resource
    private UploadService uploadService;

    @ApiOperation("上传视频")
    @PostMapping("video")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile video) {
        try {
            return new Result<>(uploadService.saveVideoFile(constantsProperties.getVideoUploadDir(),video));
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @ApiOperation("删除视频")
    @DeleteMapping("/video/{filename}")
    public Result<Null> deleteVideo(@PathVariable("filename") String filename) {
        uploadService.deleteFile(constantsProperties.getVideoUploadDir(), filename);
        return Result.ok();
    }

    @ApiOperation(value = "上传头像",notes =
            "前端会获得字符串类型的fileName,其实是文件保存路径" +
            "用户在更新自己的头像时,会先上传, 然后用户点击确定后," +
            "前端调用更新User的api,参数带上这次存在的返回结果." +
            "用户上传了文件之后,如果没有确认而是退出, 记得删除这张图片")
    @PostMapping("/icon")
    public Result<String> uploadIcon(@RequestParam("file") MultipartFile image) {
        try {
            return new Result<>(uploadService.saveImageFile(constantsProperties.getImageUploadDir(), image));
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @ApiOperation("删除头像")
    @DeleteMapping("/icon/{filename}")
    public Result<Null> deleteIcon(@PathVariable("filename") String filename) {
        uploadService.deleteFile(constantsProperties.getImageUploadDir(), filename);
        return Result.ok();
    }


}

