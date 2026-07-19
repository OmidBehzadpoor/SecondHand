package com.example.secondhand.exception;

public class InvalidImageException extends RuntimeException {
    public InvalidImageException(String message) {
        super(message);
    }
}