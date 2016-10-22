package com.di7ak.spaces.api;

public class SpacesException extends Exception {
    public int code = 0;
    public String captchaUrl;
    
    public SpacesException(int code) {
        super(Codes.getByCode(code));
        this.code = code;
    }
    
    public SpacesException(int code, String captchaUrl) {
        super(Codes.getByCode(code));
        this.code = code;
        this.captchaUrl = captchaUrl;
    }
    
    
}
