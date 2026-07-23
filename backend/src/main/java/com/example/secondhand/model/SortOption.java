package com.example.secondhand.model;

/**
 * <h2>SortOption</h2>
 * <p>
 * شمارشی (Enum) نماینده‌ی گزینه‌های ممکن برای <b>مرتب‌سازی</b> نتایج جست‌وجوی
 * آگهی‌ها، مورد استفاده در
 * {@link com.example.secondhand.service.AdvertisementService#getAll(String, Long, Long, Long, Long, SortOption, org.springframework.data.domain.Pageable)}.
 * </p>
 *
 * @author تیم بک‌اند
 */
public enum SortOption {
    /** مرتب‌سازی بر اساس جدیدترین آگهی‌ها. */
    NEWEST,
    /** مرتب‌سازی بر اساس قدیمی‌ترین آگهی‌ها. */
    OLDEST,
    /** مرتب‌سازی بر اساس قیمت، از کم به زیاد. */
    PRICE_ASC,
    /** مرتب‌سازی بر اساس قیمت، از زیاد به کم. */
    PRICE_DESC
}
