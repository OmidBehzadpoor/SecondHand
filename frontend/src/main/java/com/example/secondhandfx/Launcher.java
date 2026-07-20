package com.example.secondhandfx;

/**
 * نقطه‌ی ورود واقعی برنامه.
 * <p>
 * دلیل وجود این کلاس: وقتی متد {@code main} داخل کلاسی باشد که خودش
 * زیرکلاس {@code javafx.application.Application} است (مثل {@link MainApplication})،
 * و برنامه مستقیماً با {@code java -jar ...} یا از طریق IDE (بدون
 * {@code javafx-maven-plugin}) اجرا شود، JVM ماژول‌های JavaFX را روی
 * module-path پیدا نمی‌کند و خطای زیر رخ می‌دهد:
 * <pre>Error: JavaFX runtime components are missing, and are required to run this application</pre>
 * با جدا کردن {@code main} به یک کلاس ساده که Application را extend نمی‌کند،
 * JVM دیگر این کلاس را به‌عنوان یک اپلیکیشن JavaFX تشخیص نمی‌دهد و بارگذاری
 * ماژول‌های JavaFX به‌صورت معمولی (از طریق classpath) انجام می‌شود.
 */
public class Launcher {

    public static void main(String[] args) {
        MainApplication.main(args);
    }
}