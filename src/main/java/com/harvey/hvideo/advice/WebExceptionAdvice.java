package com.harvey.hvideo.advice;


import com.harvey.hvideo.exception.BadRequestException;
import com.harvey.hvideo.exception.UnauthorizedException;
import com.harvey.hvideo.pojo.vo.Null;
import com.harvey.hvideo.pojo.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 异常处理增强
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 17:31
 */
@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public Result<Null> handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return Result.fail(500,"服务器异常,请稍后再试 ");
    }
    @ExceptionHandler(BadRequestException.class)
    public Result<Null> handleBadRequestException(BadRequestException bre){
        return new Result<>(bre.getCode(), bre.getMessage());
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Null> handleBadRequestException(MethodArgumentTypeMismatchException e){
        return new Result<>(403,"请求方式错误或URL参数格式不符合要求");
    }
    @ExceptionHandler(UnauthorizedException.class)
    public Result<Null> handleUnauthorizedExceptionException(UnauthorizedException e){
        return new Result<>(401,e.getMessage());
    }
}
