package com.harvey.hvideo.exception;

/**
 * 权限Exception
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-01 14:09
 */
public class ForbiddenException extends BadRequestException{
    @Override
    public int getCode() {
        return 403;
    }
    public ForbiddenException(){
        super();
    }
    public ForbiddenException(String message){
        super(message);
    }
    public ForbiddenException(String message,Throwable cause){
        super(message,cause);
    }
    public ForbiddenException(Throwable cause){
        super(cause);
    }
}
