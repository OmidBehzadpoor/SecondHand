package com.example.secondhand.exception;

public class InvalidCategoryHierarchyException extends RuntimeException {
    public InvalidCategoryHierarchyException(String message) {
        super(message);
    }
}
