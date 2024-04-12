package com.harvey.hvideo.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录必要的信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:05
 */
@Data
@ApiModel(description = "注册时需要的参数")
public class RegisterFormDto implements Serializable {
    @ApiModelProperty("用户的电话号码,会在后端做正则的校验")
    private String phone;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("昵称")
    private String nickName;
    @ApiModelProperty("头像")
    private String icon;
}

