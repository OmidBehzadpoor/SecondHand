package com.example.secondhand.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <h2>SecurityBeansConfig</h2>
 * <p>
 * کلاس پیکربندی مسئول تعریف Bean های عمومی مرتبط با امنیت که در سایر
 * بخش‌های برنامه (مانند {@link com.example.secondhand.service.UserService})
 * از طریق تزریق وابستگی استفاده می‌شوند.
 * </p>
 *
 * @author تیم بک‌اند
 */
@Configuration
public class SecurityBeansConfig {

    /**
     * تعریف Bean رمزنگار رمز عبور مبتنی بر الگوریتم <b>BCrypt</b>.
     * <p>
     * این Bean در سراسر برنامه برای هش کردن رمز عبور هنگام ثبت‌نام و مقایسه‌ی
     * رمز عبور هنگام ورود استفاده می‌شود.
     * </p>
     *
     * @return یک نمونه از {@link BCryptPasswordEncoder} به‌عنوان پیاده‌سازی {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
