package com.example.secondhand.repository;

import com.example.secondhand.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <h2>CategoryRepository</h2>
 * <p>
 * ریپازیتوری Spring Data JPA برای موجودیت {@link Category}. علاوه بر
 * عملیات پایه‌ی CRUD فراهم‌شده توسط {@link JpaRepository}، شامل متدهای
 * مشتق‌شده (Derived Query) برای کار با ساختار درختی دسته‌بندی‌ها است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.CategoryService
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * دریافت لیست دسته‌بندی‌های ریشه (بدون والد)، صرف‌نظر از فعال یا غیرفعال بودن.
     *
     * @return لیست دسته‌بندی‌های ریشه
     */
    List<Category> findByParentIsNull();

    /**
     * دریافت لیست دسته‌بندی‌های ریشه (بدون والد) که <b>فعال</b> هستند.
     *
     * @return لیست دسته‌بندی‌های ریشه‌ی فعال
     */
    List<Category> findByParentIsNullAndActiveTrue();

    /**
     * بررسی وجود حداقل یک زیردسته برای دسته‌بندی مشخص‌شده.
     *
     * @param parentId شناسه دسته‌بندی والد
     * @return {@code true} در صورتی که حداقل یک زیردسته با این والد وجود داشته باشد
     */
    boolean existsByParentId(Long parentId);
}
