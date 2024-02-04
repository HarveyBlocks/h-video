package com.harvey.hvideo.service.impl;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.harvey.hvideo.exception.BadRequestException;
import com.harvey.hvideo.pojo.vo.FileWithUserId;
import com.harvey.hvideo.properties.AuthProperties;
import com.harvey.hvideo.properties.ConstantsProperties;
import com.harvey.hvideo.service.UploadService;
import com.harvey.hvideo.util.RedisConstants;
import com.harvey.hvideo.util.RedissonLock;
import com.harvey.hvideo.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-02 09:52
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {


    @Override
    public void deleteFile(final String constDir, String filename) {
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
    public String saveVideoFile(final String constDir, MultipartFile file) throws IOException {
        // 获取原始文件名称
        String originalFilename = file.getOriginalFilename();
        // 生成新文件名
        String fileName = createNewFileName(constDir, originalFilename);
        // RE
        boolean added = ORDER_QUEUE.add(
                new FileWithUserId(file, new File(constDir, fileName), UserHolder.currentUserId()));
        if (added) {
            log.debug("文件上传成功，{}", fileName);
        } else {
            log.error("文件上传失败，{}", fileName);
        }
        // 返回结果
        return fileName;
    }

    @Override
    public String saveImageFile(final String constDir, MultipartFile file) throws IOException {
        // 获取原始文件名称
        String originalFilename = file.getOriginalFilename();
        // 生成新文件名
        String fileName = createNewFileName(constDir, originalFilename);
        // RE
        file.transferTo(new File(constDir, fileName));
        log.debug("文件上传成功，{}", fileName);
        return fileName;
    }


    // 线程池
    private static final ExecutorService SAVE_VIDEO_FILE_EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * 异步保存视频<br>
     * 由于这一任务就好像垃圾处理机制一样, 需要时刻准备下单, 需要在服务器一起动时就开启<br>
     * 故使用了@PostConstruct
     *
     * @see javax.annotation.PostConstruct
     */
    @PostConstruct
    private void asynchronousSaveVideoFile() {
        SAVE_VIDEO_FILE_EXECUTOR.submit(
                this::saveVideoFileByQueue
        );
    }

    private static final BlockingQueue<FileWithUserId> ORDER_QUEUE = new ArrayBlockingQueue<>(1024 * 1024/*指定队列长度*/);


    public void saveVideoFileByQueue() {
        while (true) {
            try {
                // 获取队列中的订单信息
                FileWithUserId fileWithUserId = ORDER_QUEUE.take();
                handleVideoFile(fileWithUserId);
            } catch (InterruptedException ie) {
                log.error("处理订单发生线程异常", ie);
            } catch (Exception e) {
                log.error("处理订单发生其他异常", e);
            }
        }
    }

    private final RedissonLock<Exception> redissonLock;

    public UploadServiceImpl(RedissonLock<Exception> redissonLock) {
        this.redissonLock = redissonLock;
    }

    private void handleVideoFile(FileWithUserId fileWithUserId) throws InterruptedException {
        if (fileWithUserId == null) {
            return;
        }
        MultipartFile file = fileWithUserId.getFile();
        long userId = fileWithUserId.getUserId();
        File target = fileWithUserId.getTarget();
        Exception exception = redissonLock
                .asynchronousLock(RedisConstants.VIDEO_UPLOAD_LOCK + userId,
                        () -> {
                            try {
                                file.transferTo(target);
                            } catch (Exception e) {
                                return e;
                            }
                            return null;
                        }
                );
        if (exception != null) {
            log.error("用户:`" + fileWithUserId.getUserId() + "`的视频`" +
                    fileWithUserId.getTarget().getAbsolutePath() +
                    "`保存失败", exception);
        }
    }
}