package com.example.secondhand.exception;

public class AdvertisementImageNotFoundException extends RuntimeException {
    public AdvertisementImageNotFoundException(String message) {
        super(message);
    }
}