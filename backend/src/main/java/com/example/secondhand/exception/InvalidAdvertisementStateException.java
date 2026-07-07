package com.example.secondhand.exception;

public class InvalidAdvertisementStateException extends RuntimeException {
    public InvalidAdvertisementStateException(String message) {
        super(message);
    }
}