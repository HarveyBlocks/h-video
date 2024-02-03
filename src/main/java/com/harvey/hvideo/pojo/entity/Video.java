package com.harvey.hvideo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
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
@TableName("tb_video")
@ApiModel(description = "视频信息")
public class Video implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty("视频主键")
    private Long id;

    @ApiModelProperty("作者id")
    private Long userId;

    @ApiModelProperty("作者图标")
    @TableField(exist = false)
    private String icon;

    @ApiModelProperty("作者昵称")
    @TableField(exist = false)
    private String nickName;

    @ApiModelProperty("视频标题")
    private String title;

    @ApiModelProperty("视频地址")
    @TableField("`video_path`")
    private String videoPath;

    @ApiModelProperty("点击量")
    private Long click;

    @ApiModelProperty("评论量")
    private Integer comments;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
