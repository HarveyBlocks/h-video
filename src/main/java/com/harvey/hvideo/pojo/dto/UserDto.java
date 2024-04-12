package com.harvey.hvideo.pojo.dto;

import com.harvey.hvideo.pojo.entity.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户简要信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:13
 */
@Data
@AllArgsConstructor
@ApiModel(description = "简单的用户信息")
public class UserDto implements Serializable {
    @ApiModelProperty(value = "用户权限",example = "1为普通用户,2为vip(没有)")
    private Integer role;
    @ApiModelProperty("用户主键")
    private Long id;
    @ApiModelProperty("昵称")
    private String nickName;
    @ApiModelProperty("头像的地址,会保存在静态资源的文件夹")
    private String icon;

    public UserDto() {

    }
    public UserDto(User user) {
        this.role = user.getRole().getValue();
        this.id = user.getId();
        this.nickName = user.getNickName();
        this.icon = user.getIcon();
    }
}
