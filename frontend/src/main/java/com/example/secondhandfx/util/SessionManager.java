// This class is a Singleton (private constructor, only one static instance
// is allowed), so @NoArgsConstructor/@AllArgsConstructor must not be used —
// those annotations auto-generate a public constructor, which bypasses the
// exact restriction the private constructor was meant to enforce (it would
// allow creating multiple parallel session instances).
// @Setter is also not appropriate here, since session changes must only go
// through the controlled setSession/clearSession methods, not by setting
// individual fields independently and out of sync with each other.

package com.example.secondhandfx.util;

import com.example.secondhandfx.model.Role;

/**
 * <h2>SessionManager</h2>
 * <p>
 * کلاس <b>Singleton</b> مسئول نگه‌داری اطلاعات نشست (Session) کاربر جاری در
 * طول اجرای برنامه‌ی فرانت‌اند، شامل توکن JWT، شناسه، نام کاربری، نقش و نام
 * کاربر واردشده.
 * </p>
 *
 * @author تیم فرانت‌اند
 */
public class SessionManager {

    private static final SessionManager instance = new SessionManager();

    private String token;
    private Long userId;
    private String username;
    private Role role;
    private String name;


    private SessionManager() {
    }

    /**
     * دریافت تنها نمونه‌ی (Singleton) این کلاس.
     *
     * @return نمونه‌ی واحد {@link SessionManager}
     */
    public static SessionManager getInstance() {
        return instance;
    }

    /**
     * ثبت اطلاعات نشست کاربر پس از ورود موفق.
     *
     * @param token    توکن JWT صادرشده برای کاربر
     * @param userId   شناسه کاربر
     * @param username نام کاربری
     * @param role     نقش دسترسی کاربر
     * @param name     نام کامل کاربر
     */
    public void setSession(String token, Long userId, String username, Role role, String name) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.name = name;
    }

    /**
     * پاک کردن اطلاعات نشست کاربر (مثلاً هنگام خروج از حساب کاربری).
     */
    public void clearSession() {
        this.token = null;
        this.userId = null;
        this.username = null;
        this.role = null;
        this.name = null;
    }

    /**
     * بررسی اینکه آیا در حال حاضر کاربری وارد سیستم شده است یا خیر.
     *
     * @return {@code true} در صورت وجود توکن معتبر در نشست، در غیر این صورت {@code false}
     */
    public boolean isLoggedIn() {
        return token != null;
    }

    /**
     * بررسی اینکه آیا کاربر جاری نقش ادمین دارد یا خیر.
     *
     * @return {@code true} در صورتی که نقش کاربر جاری {@link Role#ADMIN} باشد
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * دریافت توکن JWT کاربر جاری.
     *
     * @return توکن JWT، یا {@code null} در صورت عدم ورود کاربر
     */
    public String getToken() {
        return token;
    }

    /**
     * دریافت شناسه کاربر جاری.
     *
     * @return شناسه کاربر، یا {@code null} در صورت عدم ورود کاربر
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * دریافت نام کاربری کاربر جاری.
     *
     * @return نام کاربری، یا {@code null} در صورت عدم ورود کاربر
     */
    public String getUsername() {
        return username;
    }

    /**
     * دریافت نقش دسترسی کاربر جاری.
     *
     * @return نقش کاربر، یا {@code null} در صورت عدم ورود کاربر
     */
    public Role getRole() {
        return role;
    }

    /**
     * دریافت نام کامل کاربر جاری.
     *
     * @return نام کامل کاربر، یا {@code null} در صورت عدم ورود کاربر
     */
    public String getName() {
        return name;
    }

}
