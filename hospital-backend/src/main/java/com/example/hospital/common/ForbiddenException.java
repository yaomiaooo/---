package com.example.hospital.common;

/**
 * 权限不足异常
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException() {
        super();
    }
}
