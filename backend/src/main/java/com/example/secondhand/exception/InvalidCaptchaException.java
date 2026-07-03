package com.example.secondhand.exception;

public class InvalidCaptchaException extends RuntimeException {
    public InvalidCaptchaException(String message) {
        super(message);
    }
}
