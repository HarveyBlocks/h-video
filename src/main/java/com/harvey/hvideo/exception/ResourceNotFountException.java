package com.harvey.hvideo.exception;

/**
 * 404的HttpException
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-02-02 13:56
 */
public class ResourceNotFountException  extends BadRequestException{
    @Override
    public int getCode() {
        return 404;
    }
    public ResourceNotFountException(){
        super();
    }
    public ResourceNotFountException(String message){
        super(message);
    }
    public ResourceNotFountException(String message,Throwable cause){
        super(message,cause);
    }
    public ResourceNotFountException(Throwable cause){
        super(cause);
    }
}
