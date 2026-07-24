package com.example.secondhandfx.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * <h2>Config</h2>
 * <p>
 * کلاس کمکی (Utility) مسئول خواندن <b>آدرس پایه‌ی سرور بک‌اند (API Base URL)</b>
 * از فایل تنظیمات خارجی {@code config/config.properties}. در صورت نبود این
 * فایل یا خالی بودن مقدار آن، از یک آدرس پیش‌فرض استفاده می‌شود.
 * </p>
 *
 * @author تیم فرانت‌اند
 */
public class Config {

    private static final String DEFAULT_API_BASE_URL = "https://secondhand-6kfg.onrender.com";

    private static final String API_BASE_URL = loadApiBaseUrl();

    /** سازنده‌ی خصوصی برای جلوگیری از نمونه‌سازی؛ این کلاس فقط شامل متدهای استاتیک است. */
    private Config() {
    }

    /**
     * دریافت آدرس پایه‌ی سرور بک‌اند که در زمان بارگذاری کلاس تعیین شده است.
     *
     * @return آدرس پایه‌ی API (خوانده‌شده از فایل تنظیمات یا مقدار پیش‌فرض)
     */
    public static String getApiBaseUrl() {
        return API_BASE_URL;
    }

    /**
     * خواندن آدرس پایه‌ی API از فایل {@code config/config.properties}.
     * <p>
     * در صورت وجود نداشتن فایل، خالی بودن مقدار {@code API_BASE_URL}، یا بروز
     * خطا هنگام خواندن فایل، مقدار {@link #DEFAULT_API_BASE_URL} بازگردانده می‌شود.
     * </p>
     *
     * @return آدرس پایه‌ی API نهایی که باید استفاده شود
     */
    private static String loadApiBaseUrl() {
        Path externalConfig = Path.of("config", "config.properties");

        if (Files.exists(externalConfig)) {
            try (InputStream in = Files.newInputStream(externalConfig)) {
                Properties props = new Properties();
                props.load(in);
                String url = props.getProperty("API_BASE_URL");
                if (url != null && !url.isBlank()) {
                    return url.trim();
                }
            } catch (IOException e) {
                System.err.println("Could not read config/config.properties, falling back to default: " + e.getMessage());
            }
        }

        return DEFAULT_API_BASE_URL;
    }
}
