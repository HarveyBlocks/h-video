package com.harvey.hvideo.pojo.dto;


import com.harvey.hvideo.pojo.entity.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 简化的会话信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-11 15:12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RecordDto implements Serializable {

    @ApiModelProperty(value = "有消息记录的好友的信息")
    private UserDto userDto;
    @ApiModelProperty(value = "该消息记录的ID")
    private long id;
    public RecordDto(long id, User user) {
        this.userDto = new UserDto(user);
        this.id = id;
    }
}
