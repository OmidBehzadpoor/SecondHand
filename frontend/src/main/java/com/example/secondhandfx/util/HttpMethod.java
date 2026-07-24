package com.example.secondhandfx.util;

/**
 * <h2>HttpMethod</h2>
 * <p>
 * شمارشی (Enum) نماینده‌ی متدهای HTTP پشتیبانی‌شده توسط {@link HttpClientHelper}
 * برای ارتباط با بک‌اند.
 * </p>
 *
 * @author تیم فرانت‌اند
 */
public enum HttpMethod {
    /** درخواست دریافت اطلاعات. */
    GET,
    /** درخواست ایجاد یک منبع جدید. */
    POST,
    /** درخواست جایگزینی کامل یک منبع موجود. */
    PUT,
    /** درخواست به‌روزرسانی جزئی یک منبع موجود. */
    PATCH,
    /** درخواست حذف یک منبع. */
    DELETE
}
