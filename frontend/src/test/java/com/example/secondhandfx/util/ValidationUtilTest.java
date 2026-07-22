package com.example.secondhandfx.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationUtilTest {

    // ==================== isBlank ====================

    @Test
    void isBlank_shouldReturnTrue_whenValueIsNull() {
        assertTrue(ValidationUtil.isBlank(null));
    }

    @Test
    void isBlank_shouldReturnTrue_whenValueIsEmptyString() {
        assertTrue(ValidationUtil.isBlank(""));
    }

    @Test
    void isBlank_shouldReturnTrue_whenValueIsOnlyWhitespace() {
        assertTrue(ValidationUtil.isBlank("   "));
    }

    @Test
    void isBlank_shouldReturnFalse_whenValueHasContent() {
        assertFalse(ValidationUtil.isBlank("Ali"));
    }

    // ==================== isValidEmail ====================

    @Test
    void isValidEmail_shouldReturnTrue_whenFormatIsValid() {
        assertTrue(ValidationUtil.isValidEmail("ali@example.com"));
    }

    @Test
    void isValidEmail_shouldReturnTrue_whenEmailHasDotsAndDashes() {
        assertTrue(ValidationUtil.isValidEmail("ali.reza-1@my-example.co"));
    }

    @Test
    void isValidEmail_shouldReturnFalse_whenMissingAtSymbol() {
        assertFalse(ValidationUtil.isValidEmail("ali.example.com"));
    }

    @Test
    void isValidEmail_shouldReturnFalse_whenMissingDomain() {
        assertFalse(ValidationUtil.isValidEmail("ali@"));
    }

    @Test
    void isValidEmail_shouldReturnFalse_whenNull() {
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    void isValidEmail_shouldReturnFalse_whenTopLevelDomainIsTooShort() {
        assertFalse(ValidationUtil.isValidEmail("ali@example.c"));
    }

    // ==================== isValidPhone ====================

    @Test
    void isValidPhone_shouldReturnTrue_whenFormatIsValidIranianMobile() {
        assertTrue(ValidationUtil.isValidPhone("09121234567"));
    }

    @Test
    void isValidPhone_shouldReturnFalse_whenDoesNotStartWithZeroNine() {
        assertFalse(ValidationUtil.isValidPhone("08121234567"));
    }

    @Test
    void isValidPhone_shouldReturnFalse_whenTooShort() {
        assertFalse(ValidationUtil.isValidPhone("0912123456"));
    }

    @Test
    void isValidPhone_shouldReturnFalse_whenTooLong() {
        assertFalse(ValidationUtil.isValidPhone("091212345678"));
    }

    @Test
    void isValidPhone_shouldReturnFalse_whenContainsNonDigits() {
        assertFalse(ValidationUtil.isValidPhone("0912123456a"));
    }

    @Test
    void isValidPhone_shouldReturnFalse_whenNull() {
        assertFalse(ValidationUtil.isValidPhone(null));
    }
}
