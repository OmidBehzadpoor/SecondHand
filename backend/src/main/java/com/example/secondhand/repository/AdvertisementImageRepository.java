package com.example.secondhand.repository;

import com.example.secondhand.model.AdvertisementImage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <h2>AdvertisementImageRepository</h2>
 * <p>
 * ریپازیتوری Spring Data JPA برای موجودیت {@link AdvertisementImage}. تمام
 * عملیات پایه‌ی CRUD (ذخیره، حذف، جست‌وجو بر اساس شناسه و غیره) از طریق
 * {@link JpaRepository} به‌صورت خودکار فراهم می‌شوند و متد سفارشی اضافه‌ای
 * برای این موجودیت مورد نیاز نیست.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.AdvertisementImageService
 */
public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImage, Long> {
}
