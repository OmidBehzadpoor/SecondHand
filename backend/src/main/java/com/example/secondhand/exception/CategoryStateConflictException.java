package com.example.secondhand.exception;

public class CategoryStateConflictException extends RuntimeException {
    public CategoryStateConflictException(String message) {
        super(message);
    }
}
