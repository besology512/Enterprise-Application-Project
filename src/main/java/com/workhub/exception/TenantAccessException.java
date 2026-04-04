package com.workhub.exception;

public class TenantAccessException extends RuntimeException {
    public TenantAccessException(String message) {
        super(message);
    }
}