package com.example.secondhand.exception;

public class UserStateConflictException extends RuntimeException {
    public UserStateConflictException(String message) {
        super(message);
    }
}
