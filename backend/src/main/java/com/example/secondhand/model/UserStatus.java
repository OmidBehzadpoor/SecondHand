package com.example.secondhand.model;

/**
 * <h2>UserStatus</h2>
 * <p>
 * شمارشی (Enum) نماینده‌ی وضعیت حساب کاربری یک {@link User} در سامانه.
 * </p>
 *
 * @author تیم بک‌اند
 */
public enum UserStatus {
    /** حساب کاربری فعال و بدون محدودیت است. */
    ACTIVE,
    /** حساب کاربری توسط ادمین مسدود شده است. */
    BLOCKED
}

