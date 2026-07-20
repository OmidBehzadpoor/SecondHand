package com.example.secondhandfx.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // فرمت ساده و متداول ایمیل — چک دقیق نهایی همیشه سمت بک‌اند انجام می‌شود
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    // شماره موبایل ایرانی: با 09 شروع شود و ۱۱ رقم باشد
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^09\\d{9}$");

    private ValidationUtil() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}