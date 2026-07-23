package com.example.secondhand.controller;

import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.AdminRejectRequest;
import com.example.secondhand.dto.response.AdminAdvertisementResponse;
import com.example.secondhand.service.AdvertisementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * <h2>AdminAdvertisementController</h2>
 * <p>
 * کنترلر مخصوص پنل مدیریت (ادمین) برای <b>بررسی، تایید، رد و حذف آگهی‌ها</b>.
 * تمام اندپوینت‌های این کنترلر تحت مسیر پایه {@code /api/admin/advertisements}
 * قرار دارند و فقط برای کاربرانی با نقش {@code ADMIN} در دسترس هستند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.AdvertisementService
 */
@RestController
@RequestMapping("/api/admin/advertisements")
@RequiredArgsConstructor
@Tag(name = "Admin - Advertisements", description = "بررسی، تایید و رد آگهی‌ها توسط مدیر - نیاز به توکن مدیر")
public class AdminAdvertisementController {

    private final AdvertisementService advertisementService;

    /**
     * دریافت لیست تمام آگهی‌های در انتظار بررسی.
     *
     * @return {@link ResponseEntity} حاوی لیست {@link AdminAdvertisementResponse} با وضعیت {@code PENDING}
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminAdvertisementResponse>>> getPending() {
        List<AdminAdvertisementResponse> responses = advertisementService.getPendingAdvertisements();
        return ResponseEntity.ok(new ApiResponse<>(true, "PENDING_ADVERTISEMENTS_RETRIEVED", responses));
    }

    /**
     * تایید یک آگهی در انتظار بررسی.
     *
     * @param id شناسه آگهی‌ای که باید تایید شود
     * @return {@link ResponseEntity} حاوی {@link AdminAdvertisementResponse} به‌روزشده پس از تایید
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminAdvertisementResponse>> approve(@PathVariable Long id) {
        AdminAdvertisementResponse response = advertisementService.approve(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_APPROVED", response));
    }

    /**
     * رد یک آگهی در انتظار بررسی، به‌همراه ذکر دلیل رد.
     *
     * @param id      شناسه آگهی‌ای که باید رد شود
     * @param request بدنه‌ی درخواست حاوی دلیل رد آگهی
     * @return {@link ResponseEntity} حاوی {@link AdminAdvertisementResponse} به‌روزشده پس از رد
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminAdvertisementResponse>> reject(
            @PathVariable Long id, @Valid @RequestBody AdminRejectRequest request) {
        AdminAdvertisementResponse response = advertisementService.reject(id, request.getReason());
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_REJECTED", response));
    }

    /**
     * حذف نرم (Soft Delete) یک آگهی توسط ادمین.
     *
     * @param id شناسه آگهی‌ای که باید حذف شود
     * @return {@link ResponseEntity} حاوی پیام موفقیت‌آمیز بودن عملیات حذف
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        advertisementService.adminDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_DELETED_BY_ADMIN", null));
    }

    /**
     * دریافت لیست تمام آگهی‌های سامانه (بدون فیلتر وضعیت).
     *
     * @return {@link ResponseEntity} حاوی لیست کامل {@link AdminAdvertisementResponse}
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminAdvertisementResponse>>> getAll() {
        List<AdminAdvertisementResponse> responses = advertisementService.getAllForAdmin();
        return ResponseEntity.ok(new ApiResponse<>(true, "ALL_ADVERTISEMENTS_RETRIEVED", responses));
    }

}
