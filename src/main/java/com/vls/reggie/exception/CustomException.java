package com.vls.reggie.exception;

public class CustomException extends RuntimeException{

    public CustomException(String message){
        //父类构造函数
        super(message);
    }
}
