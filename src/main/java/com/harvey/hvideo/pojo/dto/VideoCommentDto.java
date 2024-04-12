package com.harvey.hvideo.pojo.dto;

import com.harvey.hvideo.pojo.entity.User;
import com.harvey.hvideo.pojo.entity.VideoComment;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-04 12:06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "视频评论")
public class VideoCommentDto implements Serializable {
    @ApiModelProperty("发布者nickName")
    private String nickName;

    @ApiModelProperty("发布者头像")
    private String icon;

    @ApiModelProperty("关联的1级评论用户昵称，如果是一级评论，则值为null")
    private String parentNickName;

    @ApiModelProperty("回复的内容")
    private String content;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    public VideoCommentDto(VideoComment videoComment, User user, String parentUserName){
        this.createTime = videoComment.getCreateTime();
        this.content = videoComment.getContent();
        this.nickName = user.getNickName();
        this.icon = user.getIcon();
        if (parentUserName!=null&&!parentUserName.isEmpty()){
            this.parentNickName = parentUserName;
        }
    }
    public VideoCommentDto(VideoComment videoComment, User user){
        this(videoComment,user,null);
    }
}
