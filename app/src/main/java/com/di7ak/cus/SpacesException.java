package com.di7ak.cus;

public class SpacesException extends Exception {
    public int code = 0;
    
    public SpacesException(int code) {
        super(Codes.getByCode(code));
        this.code = code;
    }
}
