package com.example.secondhand.controller;

import com.example.secondhand.dto.response.AdminUserResponse;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h2>AdminUserController</h2>
 * <p>
 * کنترلر مخصوص پنل مدیریت (ادمین) برای <b>مدیریت کاربران</b>، شامل مشاهده لیست
 * کاربران و مسدودسازی/رفع مسدودیت آن‌ها. تمام اندپوینت‌های این کنترلر تحت
 * مسیر پایه {@code /api/admin/users} قرار دارند و فقط برای کاربرانی با نقش
 * {@code ADMIN} در دسترس هستند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.UserService
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "مدیریت کاربران توسط مدیر - نیاز به توکن مدیر")
public class AdminUserController {

    private final UserService userService;

    /**
     * دریافت لیست تمام کاربران سامانه.
     *
     * @return {@link ResponseEntity} حاوی لیست {@link AdminUserResponse} تمام کاربران
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAllUsers() {
        List<AdminUserResponse> users = userService.getAllUsersForAdmin();
        return ResponseEntity.ok(new ApiResponse<>(true, "USERS_FETCHED", users));
    }

    /**
     * مسدود کردن یک کاربر.
     *
     * @param userId شناسه کاربری که باید مسدود شود
     * @return {@link ResponseEntity} حاوی {@link AdminUserResponse} به‌روزشده پس از مسدودسازی
     */
    @PatchMapping("/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserResponse>> blockUser(@PathVariable Long userId) {
        AdminUserResponse response = userService.blockUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "USER_BLOCKED", response));
    }

    /**
     * رفع مسدودیت یک کاربر.
     *
     * @param userId شناسه کاربری که باید از حالت مسدود خارج شود
     * @return {@link ResponseEntity} حاوی {@link AdminUserResponse} به‌روزشده پس از رفع مسدودیت
     */
    @PatchMapping("/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserResponse>> unblockUser(@PathVariable Long userId) {
        AdminUserResponse response = userService.unblockUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "USER_UNBLOCKED", response));
    }
}
