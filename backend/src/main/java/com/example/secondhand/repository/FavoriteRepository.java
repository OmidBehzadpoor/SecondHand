package com.example.secondhand.repository;

import com.example.secondhand.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <h2>FavoriteRepository</h2>
 * <p>
 * ریپازیتوری Spring Data JPA برای موجودیت {@link Favorite}. علاوه بر
 * عملیات پایه‌ی CRUD فراهم‌شده توسط {@link JpaRepository}، شامل متدهای
 * مشتق‌شده (Derived Query) برای مدیریت علاقه‌مندی‌های یک کاربر است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.FavoriteService
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * دریافت لیست تمام آگهی‌های موردعلاقه‌ی یک کاربر مشخص.
     *
     * @param userId شناسه کاربر
     * @return لیست علاقه‌مندی‌های ثبت‌شده توسط این کاربر
     */
    List<Favorite> findByUserId(Long userId);

    /**
     * بررسی وجود یک رکورد علاقه‌مندی برای ترکیب مشخصی از کاربر و آگهی.
     *
     * @param userId          شناسه کاربر
     * @param advertisementId شناسه آگهی
     * @return {@code true} در صورتی که این آگهی از قبل در علاقه‌مندی‌های این کاربر باشد
     */
    boolean existsByUserIdAndAdvertisementId(Long userId, Long advertisementId);

    /**
     * حذف رکورد علاقه‌مندی متناظر با ترکیب مشخصی از کاربر و آگهی.
     *
     * @param userId          شناسه کاربر
     * @param advertisementId شناسه آگهی
     */
    void deleteByUserIdAndAdvertisementId(Long userId, Long advertisementId);
}
