package com.harvey.hvideo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tb_video_comment")
public class VideoComment implements Serializable {
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("发布者id,即发起请求者")
    private Long userId;

    @ApiModelProperty("视频id")
    private Long videoId;

    @ApiModelProperty("关联的1级评论id，如果是一级评论，则值为0")
    private Long parentId;

    @ApiModelProperty("回复的内容")
    private String content;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
