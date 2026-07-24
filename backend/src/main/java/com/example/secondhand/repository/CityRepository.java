package com.example.secondhand.repository;

import com.example.secondhand.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <h2>CityRepository</h2>
 * <p>
 * ریپازیتوری Spring Data JPA برای موجودیت {@link City}. تمام عملیات پایه‌ی
 * CRUD (ذخیره، حذف، جست‌وجو بر اساس شناسه و غیره) از طریق {@link JpaRepository}
 * به‌صورت خودکار فراهم می‌شوند و متد سفارشی اضافه‌ای برای این موجودیت مورد
 * نیاز نیست.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.CityService
 */
public interface CityRepository extends JpaRepository<City, Long> {
}
