package com.example.secondhand.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * <h2>AdvertisementImage</h2>
 * <p>
 * موجودیت (Entity) نگاشت‌شده به جدول {@code advertisement_images}، نماینده‌ی
 * یک <b>تصویر متعلق به یک آگهی</b>. هر آگهی می‌تواند چندین تصویر داشته باشد
 * (رابطه‌ی چند-به-یک از تصویر به آگهی).
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.Advertisement
 * @see com.example.secondhand.service.AdvertisementImageService
 */
@Entity
@Table(name = "advertisement_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementImage
{

    /** شناسه یکتای تصویر (کلید اصلی، تولید خودکار). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** آدرس (URL) نسبی‌ای که تصویر از طریق آن در دسترس است. */
    @Column(nullable = false)
    private String imageUrl;

    /** آگهی‌ای که این تصویر به آن تعلق دارد. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private Advertisement advertisement;

}
