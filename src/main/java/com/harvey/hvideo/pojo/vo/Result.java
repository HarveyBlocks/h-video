package com.harvey.hvideo.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 返回结果的封装
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:11
 */
@Data
@AllArgsConstructor
@ApiModel(description = "返回结果封装")
public class Result<T> {

    @ApiModelProperty(
            value = "是否成功",
            notes = "true,携带data;false,携带errorMsg")
    private Integer code;
    @ApiModelProperty(
            value = "错误信息", name = "error message",
            example = "发生了一个异常!")
    private String msg;
    @ApiModelProperty(
            value = "成功后响应的数据主体",
            notes = "成功后返回前端需要的数据.如果是列表, 则会携带total")
    private T data;

    public Result() {
        this.code = 200;
    }
    public Result(T data) {
        this.code = 200;
        this.data = data;
    }
    public Result(Integer code,String errorMsg) {
        this.code = code;
        this.msg = errorMsg;
    }

    public static Result<Null> ok() {
        return new Result<>(200, null, null);
    }
}
