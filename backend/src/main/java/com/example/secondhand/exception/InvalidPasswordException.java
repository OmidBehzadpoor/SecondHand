package com.example.secondhand.exception;

/**
 * @deprecated این اکسپشن به دلیل ضعف امنیتی (User Enumeration) منسوخ شده است.
 * به جای آن از {@link InvalidCredentialsException} استفاده کنید.
 */
@Deprecated(since = "2026-07", forRemoval = true)
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}