package com.example.secondhand.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * <h2>User</h2>
 * <p>
 * موجودیت (Entity) نگاشت‌شده به جدول {@code users}، نماینده‌ی یک <b>کاربر</b>
 * در سامانه. این موجودیت هم به‌عنوان مدل داده و هم (از طریق
 * {@code @AuthenticationPrincipal}) به‌عنوان نماینده‌ی کاربر احراز هویت‌شده
 * در فرآیند امنیتی برنامه استفاده می‌شود.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.UserService
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** شناسه یکتای کاربر (کلید اصلی، تولید خودکار). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** نام کامل کاربر. */
    @Column(nullable = false)
    private String name;

    /** نام کاربری؛ باید در سامانه یکتا باشد و برای ورود استفاده می‌شود. */
    @Column(nullable = false, unique = true)
    private String username;

    /** رمز عبور هش‌شده‌ی کاربر (با استفاده از BCrypt). */
    @Column(nullable = false)
    private String password;

    /** شماره تماس کاربر؛ باید در سامانه یکتا باشد. */
    @Column(nullable = false, unique = true)
    private String phone;

    /** آدرس ایمیل کاربر. */
    @Column(nullable = false)
    private String email;

    /** نقش دسترسی کاربر؛ به‌طور پیش‌فرض {@link Role#USER} است. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    /** وضعیت حساب کاربری؛ به‌طور پیش‌فرض {@link UserStatus#ACTIVE} است. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
}
