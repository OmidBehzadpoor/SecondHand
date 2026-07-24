package com.example.secondhand.repository;

import com.example.secondhand.model.User;
import com.example.secondhand.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * <h2>UserRepository</h2>
 * <p>
 * ریپازیتوری Spring Data JPA برای موجودیت {@link User}. علاوه بر عملیات
 * پایه‌ی CRUD فراهم‌شده توسط {@link JpaRepository}، شامل متدهای مشتق‌شده
 * (Derived Query) برای احراز هویت (یافتن کاربر بر اساس نام کاربری) و
 * بررسی‌های تکراری بودن هنگام ثبت‌نام است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.UserService
 * @see com.example.secondhand.security.JwtAuthenticationFilter
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * یافتن کاربر بر اساس نام کاربری.
     *
     * @param username نام کاربری مورد جست‌وجو
     * @return کاربر یافت‌شده در قالب {@link Optional}، یا خالی در صورت عدم وجود
     */
    Optional<User> findByUsername(String username);

    /**
     * بررسی تکراری بودن یک نام کاربری در سامانه.
     *
     * @param username نام کاربری مورد بررسی
     * @return {@code true} در صورتی که این نام کاربری قبلاً ثبت شده باشد
     */
    boolean existsByUsername(String username);

    /**
     * بررسی تکراری بودن یک شماره تماس در سامانه.
     *
     * @param phone شماره تماس مورد بررسی
     * @return {@code true} در صورتی که این شماره تماس قبلاً ثبت شده باشد
     */
    boolean existsByPhone(String phone);

    /**
     * شمارش تعداد کاربران دارای وضعیت مشخص.
     *
     * @param status وضعیت مورد نظر
     * @return تعداد کاربران با این وضعیت
     */
    long countByStatus(UserStatus status);
}
