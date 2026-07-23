package com.example.secondhand.model;
import  jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * <h2>SellerRating</h2>
 * <p>
 * موجودیت (Entity) نگاشت‌شده به جدول {@code seller_ratings}، نماینده‌ی
 * یک <b>امتیاز</b> که یک خریدار ({@link #buyer}) بر اساس یک آگهی مشخص
 * ({@link #advertisement}) به فروشنده‌ی آن آگهی می‌دهد. برای هر ترکیب از
 * خریدار و آگهی، حداکثر یک امتیاز می‌تواند وجود داشته باشد (محدودیت یکتایی
 * روی {@code buyer_id} و {@code advertisement_id}).
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.SellerRatingService
 */
@Entity
@Table(
        name = "seller_ratings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"buyer_id", "advertisement_id"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SellerRating
{
    /** شناسه یکتای امتیاز (کلید اصلی، تولید خودکار). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** کاربری که در نقش خریدار این امتیاز را ثبت کرده است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    /** آگهی‌ای که این امتیاز بر اساس آن ثبت شده است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private Advertisement advertisement;

    /** مقدار عددی امتیاز ثبت‌شده. */
    @Column(nullable = false)
    private Integer rating;

    /** توضیح یا نظر اختیاری همراه با امتیاز. */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /** زمان ثبت امتیاز؛ پس از ایجاد قابل تغییر نیست. */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
