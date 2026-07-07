package com.example.secondhand.exception;

public class CityInUseException extends RuntimeException {
    public CityInUseException(String message) {
        super(message);
    }
}
