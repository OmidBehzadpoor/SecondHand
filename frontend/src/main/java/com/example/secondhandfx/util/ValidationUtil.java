package com.example.secondhandfx.util;

import java.util.regex.Pattern;

/**
 * <h2>ValidationUtil</h2>
 * <p>
 * کلاس کمکی (Utility) شامل قوانین ساده‌ی اعتبارسنجی سمت کلاینت برای فیلدهای
 * ورودی رایج (ایمیل، شماره تماس، خالی نبودن مقدار)، جهت ارائه‌ی بازخورد سریع
 * به کاربر پیش از ارسال درخواست به سرور.
 * </p>
 *
 * @author تیم فرانت‌اند
 */
public class ValidationUtil {

    // فرمت ساده و متداول ایمیل — چک دقیق نهایی همیشه سمت بک‌اند انجام می‌شود
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    // شماره موبایل ایرانی: با 09 شروع شود و ۱۱ رقم باشد
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^09\\d{9}$");

    /** سازنده‌ی خصوصی برای جلوگیری از نمونه‌سازی؛ این کلاس فقط شامل متدهای استاتیک است. */
    private ValidationUtil() {
    }

    /**
     * بررسی معتبر بودن فرمت ساده‌ی یک آدرس ایمیل.
     *
     * @param email مقدار ایمیل برای بررسی
     * @return {@code true} در صورتی که مقدار غیر {@code null} باشد و با الگوی
     *         ایمیل مطابقت داشته باشد
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * بررسی معتبر بودن فرمت یک شماره موبایل ایرانی (شروع با {@code 09} و ۱۱ رقم).
     *
     * @param phone مقدار شماره تماس برای بررسی
     * @return {@code true} در صورتی که مقدار غیر {@code null} باشد و با الگوی
     *         شماره موبایل ایرانی مطابقت داشته باشد
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * بررسی خالی یا {@code null} بودن یک مقدار رشته‌ای.
     *
     * @param value مقدار برای بررسی
     * @return {@code true} در صورتی که مقدار {@code null} یا خالی/فقط‌فاصله باشد
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
