package com.example.secondhand.controller;

import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.FavoriteResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h2>FavoriteController</h2>
 * <p>
 * کنترلر مدیریت <b>علاقه‌مندی‌های</b> کاربر نسبت به آگهی‌ها. تحت مسیر پایه
 * {@code /api/favorites} قرار دارد و تمام اندپوینت‌های آن نیاز به احراز هویت دارند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.FavoriteService
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
@Tag(name = "Favorites", description = "علاقه‌مندی‌های کاربر")
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * افزودن یک آگهی به لیست علاقه‌مندی‌های کاربر جاری.
     *
     * @param advertisementId شناسه آگهی‌ای که باید به علاقه‌مندی‌ها اضافه شود
     * @param currentUser     کاربر جاری
     * @return {@link ResponseEntity} با کد وضعیت {@code 201 CREATED} و اطلاعات علاقه‌مندی تازه‌ثبت‌شده
     */
    @PostMapping("/{advertisementId}")
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            @PathVariable Long advertisementId,
            @AuthenticationPrincipal User currentUser) {
        FavoriteResponse favoriteResponse = favoriteService.addFavorite(advertisementId, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "FAVORITE_ADDED", favoriteResponse));
    }

    /**
     * حذف یک آگهی از لیست علاقه‌مندی‌های کاربر جاری.
     *
     * @param advertisementId شناسه آگهی‌ای که باید از علاقه‌مندی‌ها حذف شود
     * @param currentUser     کاربر جاری
     * @return {@link ResponseEntity} حاوی پیام موفقیت‌آمیز بودن عملیات حذف
     */
    @DeleteMapping("/{advertisementId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long advertisementId,
            @AuthenticationPrincipal User currentUser) {
        favoriteService.removeFavorite(advertisementId, currentUser);
        ApiResponse<Void> response = new ApiResponse<>(true, "FAVORITE_REMOVED", null);
        return ResponseEntity.ok(response);
    }

    /**
     * دریافت لیست تمام آگهی‌های موردعلاقه‌ی کاربر جاری.
     *
     * @param currentUser کاربر جاری
     * @return {@link ResponseEntity} حاوی لیست {@link FavoriteResponse}
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getMyFavorites(
            @AuthenticationPrincipal User currentUser) {
        List<FavoriteResponse> favoriteResponses = favoriteService.getMyFavorites(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "FAVORITES_RETRIEVED", favoriteResponses));
    }

}
