package com.example.secondhand.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>Advertisement</h2>
 * <p>
 * موجودیت (Entity) نگاشت‌شده به جدول {@code advertisements}، نماینده‌ی
 * <b>آگهی خرید و فروش دست دوم</b> در سامانه. این موجودیت هسته‌ی اصلی سامانه
 * است و به {@link Category}، {@link City}، {@link User} (فروشنده) و لیستی از
 * {@link AdvertisementImage} مرتبط می‌شود.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.AdvertisementStatus
 * @see com.example.secondhand.service.AdvertisementService
 */
@Entity
@Table(name = "advertisements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Advertisement
{

    /** شناسه یکتای آگهی (کلید اصلی، تولید خودکار). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** عنوان آگهی. */
    @Column(nullable = false)
    private String title;

    /** توضیحات کامل آگهی. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** قیمت آگهی. */
    @Column(nullable = false)
    private Long price;

    /** زمان ایجاد آگهی؛ به‌طور پیش‌فرض زمان لحظه‌ی ساخت شیء است. */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** دسته‌بندی‌ای که این آگهی در آن ثبت شده است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** کاربری که این آگهی را ثبت کرده است (فروشنده). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User seller;

    /** شهری که آگهی در آن ثبت شده است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    /** وضعیت فعلی آگهی؛ به‌طور پیش‌فرض {@link AdvertisementStatus#PENDING} است. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AdvertisementStatus status = AdvertisementStatus.PENDING;

    /** لیست تصاویر متعلق به این آگهی. */
    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AdvertisementImage> images = new ArrayList<>();

    /** دلیل رد آگهی توسط ادمین، در صورتی که آگهی رد شده باشد. */
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
}
