package com.springwork.Exception;

public class CustomException extends RuntimeException {
    private int code;
    public CustomException(int code, String message) {
        super(message);
        this.code = code;

    }
}
