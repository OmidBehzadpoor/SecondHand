package com.example.secondhand.config;

import com.example.secondhand.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <h2>SecurityConfig</h2>
 * <p>
 * کلاس اصلی پیکربندی <b>Spring Security</b> در سامانه. این کلاس زنجیره فیلترهای
 * امنیتی (Security Filter Chain) را تعریف می‌کند و مشخص می‌کند کدام مسیرها
 * عمومی هستند و کدام‌ها نیاز به احراز هویت دارند.
 * </p>
 * <p>
 * با {@code @EnableWebSecurity} پیکربندی امنیت وب و با {@code @EnableMethodSecurity}
 * امکان استفاده از {@code @PreAuthorize} روی متدهای کنترلرها فعال می‌شود. از
 * آنجا که احراز هویت با JWT انجام می‌شود، سیاست ایجاد نشست به‌صورت
 * {@link SessionCreationPolicy#STATELESS} تنظیم شده است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.security.JwtAuthenticationFilter
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * تعریف زنجیره فیلترهای امنیتی (Security Filter Chain) برنامه.
     * <p>
     * تنظیمات اصلی این متد شامل موارد زیر است:
     * </p>
     * <ul>
     *   <li>غیرفعال‌سازی CSRF، چون سامانه به‌صورت Stateless و مبتنی بر توکن است</li>
     *   <li>سیاست نشست {@code STATELESS} (بدون نشست سمت سرور)</li>
     *   <li>بازگرداندن پاسخ JSON فارسی با کد {@code 401} در صورت عدم احراز هویت</li>
     *   <li>مجاز بودن دسترسی عمومی به مسیرهای احراز هویت ({@code /api/auth/**})،
     *       Actuator، مستندات API (Swagger/Scalar)، فایل‌های آپلودی، و برخی
     *       اندپوینت‌های {@code GET} عمومی (آگهی‌ها، دسته‌بندی‌ها، شهرها)</li>
     *   <li>نیاز به احراز هویت برای تمام سایر درخواست‌ها</li>
     *   <li>افزودن {@link JwtAuthenticationFilter} پیش از
     *       {@link UsernamePasswordAuthenticationFilter} برای پردازش توکن JWT</li>
     * </ul>
     *
     * @param http شیء {@link HttpSecurity} برای پیکربندی قوانین امنیتی
     * @return {@link SecurityFilterChain} ساخته‌شده بر اساس تنظیمات تعریف‌شده
     * @throws Exception در صورت بروز خطا در فرآیند ساخت زنجیره فیلترها
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\": \"ابتدا وارد حساب کاربری شوید\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/scalar/**",
                                "/scalar"
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/advertisements/mine").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/advertisements/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/cities/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
