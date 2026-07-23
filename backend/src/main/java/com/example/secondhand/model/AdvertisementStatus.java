package com.example.secondhand.model;

/**
 * <h2>AdvertisementStatus</h2>
 * <p>
 * شمارشی (Enum) نماینده‌ی وضعیت‌های ممکن یک {@link Advertisement} در طول
 * چرخه‌ی حیات آن، از ثبت تا تایید/رد و در نهایت فروش یا حذف.
 * </p>
 *
 * @author تیم بک‌اند
 */
public enum AdvertisementStatus {
    /** آگهی ثبت شده و در انتظار بررسی توسط ادمین است. */
    PENDING,
    /** آگهی توسط ادمین تایید شده و به‌صورت عمومی قابل مشاهده است. */
    APPROVED,
    /** آگهی توسط ادمین رد شده است. */
    REJECTED,
    /** آگهی توسط فروشنده به‌عنوان فروخته‌شده علامت‌گذاری شده است. */
    SOLD,
    /** آگهی حذف (نرم) شده است. */
    DELETED
}
