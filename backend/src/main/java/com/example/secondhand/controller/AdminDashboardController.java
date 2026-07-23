package com.example.secondhand.controller;

import com.example.secondhand.dto.response.AdminDashboardResponse;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h2>AdminDashboardController</h2>
 * <p>
 * کنترلر مخصوص پنل مدیریت (ادمین) برای نمایش <b>آمار کلی سامانه</b> در داشبورد.
 * تمام اندپوینت‌های این کنترلر تحت مسیر پایه {@code /api/admin/dashboard}
 * قرار دارند و فقط برای کاربرانی با نقش {@code ADMIN} در دسترس هستند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.AdminDashboardService
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin - Dashboard", description = "آمار کلی سامانه - نیاز به توکن مدیر")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * دریافت آمار کلی سامانه برای نمایش در داشبورد ادمین.
     *
     * @return {@link ResponseEntity} حاوی {@link AdminDashboardResponse} با تمام آمار خلاصه‌شده
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        AdminDashboardResponse response = adminDashboardService.getDashboard();
        return ResponseEntity.ok(new ApiResponse<>(true, "DASHBOARD_FETCHED", response));
    }
}
