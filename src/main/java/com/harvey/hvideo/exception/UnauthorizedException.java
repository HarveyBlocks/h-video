package com.harvey.hvideo.exception;

/**
 * 认证失败异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
public class UnauthorizedException extends BadRequestException{
    @Override
    public int getCode() {
        return 404;
    }
    public UnauthorizedException(){
        super();
    }
    public UnauthorizedException(String message){
        super(message);
    }
    public UnauthorizedException(String message,Throwable cause){
        super(message,cause);
    }
    public UnauthorizedException(Throwable cause){
        super(cause);
    }
}
