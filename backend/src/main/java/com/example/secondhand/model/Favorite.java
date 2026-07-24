package com.example.secondhand.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * <h2>Favorite</h2>
 * <p>
 * موجودیت (Entity) نگاشت‌شده به جدول {@code favorites}، نماینده‌ی
 * <b>علاقه‌مندی</b> یک کاربر ({@link #user}) نسبت به یک آگهی مشخص
 * ({@link #advertisement}). برای هر ترکیب از کاربر و آگهی، حداکثر یک رکورد
 * علاقه‌مندی می‌تواند وجود داشته باشد (محدودیت یکتایی روی
 * {@code user_id} و {@code advertisement_id}).
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.FavoriteService
 */
@Entity
@Table(
        name = "favorites",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "advertisement_id"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    /** شناسه یکتای علاقه‌مندی (کلید اصلی، تولید خودکار). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** کاربری که این آگهی را به علاقه‌مندی‌های خود اضافه کرده است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** آگهی‌ای که به علاقه‌مندی‌ها اضافه شده است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private Advertisement advertisement;

    /** زمان افزودن این آگهی به علاقه‌مندی‌ها؛ پس از ایجاد قابل تغییر نیست. */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
