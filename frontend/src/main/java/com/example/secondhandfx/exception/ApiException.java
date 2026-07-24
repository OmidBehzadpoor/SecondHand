package com.example.secondhandfx.exception;

/**
 * <h2>ApiException</h2>
 * <p>
 * استثنای بازبینی‌شده (Checked Exception) نماینده‌ی هر پاسخ ناموفق دریافتی
 * از بک‌اند در فرانت‌اند. این استثنا توسط {@link com.example.secondhandfx.util.HttpClientHelper}
 * هنگام دریافت یک کد وضعیت HTTP ناموفق (خارج از بازه‌ی ۲xx) یا بروز خطای
 * شبکه پرتاب می‌شود، و پیام آن معمولاً همان پیام خطای فارسی بازگردانده‌شده
 * توسط بک‌اند است که مستقیماً برای نمایش به کاربر (مثلاً از طریق
 * {@link com.example.secondhandfx.util.AlertUtil}) مناسب است.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.util.HttpClientHelper
 */
public class ApiException extends Exception {

    private final int statusCode;

    /**
     * ساخت یک نمونه جدید از این استثنا با پیام و کد وضعیت HTTP مشخص.
     *
     * @param message    پیام خطا (معمولاً پیام دریافت‌شده از سرور)
     * @param statusCode کد وضعیت HTTP پاسخ ناموفق؛ در صورت خطای شبکه (بدون پاسخ از سرور) مقدار {@code 0}
     */
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * دریافت کد وضعیت HTTP مرتبط با این خطا.
     *
     * @return کد وضعیت HTTP پاسخ ناموفق، یا {@code 0} در صورت خطای شبکه (بدون پاسخ از سرور)
     */
    public int getStatusCode() {
        return statusCode;
    }
}
