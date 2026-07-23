package com.example.secondhand.controller;

import com.example.secondhand.dto.SellerRatingRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.SellerRatingResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.service.SellerRatingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h2>SellerRatingController</h2>
 * <p>
 * کنترلر مسئول <b>امتیازدهی به فروشندگان</b>. تحت مسیر پایه {@code /api/ratings}
 * قرار دارد. ثبت امتیاز نیاز به احراز هویت دارد؛ مشاهده‌ی امتیازها، میانگین و
 * تعداد امتیازهای یک فروشنده به‌صورت عمومی در دسترس است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.SellerRatingService
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ratings")
@Tag(name = "Seller Ratings", description = "امتیازدهی به فروشنده")
public class SellerRatingController {

    private final SellerRatingService sellerRatingService;

    /**
     * ثبت یک امتیاز جدید برای فروشنده‌ی یک آگهی.
     *
     * @param advertisementId شناسه آگهی‌ای که امتیاز برای فروشنده‌ی آن ثبت می‌شود
     * @param request         اطلاعات امتیاز شامل مقدار امتیاز و توضیح
     * @param currentUser     کاربر جاری (خریدار)
     * @return {@link ResponseEntity} با کد وضعیت {@code 201 CREATED} و اطلاعات امتیاز ثبت‌شده
     */
    @PostMapping("/advertisements/{advertisementId}")
    public ResponseEntity<ApiResponse<SellerRatingResponse>> rateAdvertisement(
            @PathVariable Long advertisementId,
            @Valid @RequestBody SellerRatingRequest request,
            @AuthenticationPrincipal User currentUser) {
        SellerRatingResponse response = sellerRatingService.rateAdvertisement(
                advertisementId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "RATING_CREATED", response));
    }

    /**
     * دریافت لیست تمام امتیازهای ثبت‌شده برای یک فروشنده مشخص.
     *
     * @param sellerId شناسه فروشنده مورد نظر
     * @return {@link ResponseEntity} حاوی لیست {@link SellerRatingResponse}
     */
    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<List<SellerRatingResponse>>> getSellerRatings(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "RATINGS_RETRIEVED",
                sellerRatingService.getSellerRatings(sellerId)));
    }

    /**
     * دریافت میانگین امتیازهای یک فروشنده مشخص.
     *
     * @param sellerId شناسه فروشنده مورد نظر
     * @return {@link ResponseEntity} حاوی میانگین امتیازهای این فروشنده
     */
    @GetMapping("/sellers/{sellerId}/average")
    public ResponseEntity<ApiResponse<Double>> getSellerAverageRating(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "AVERAGE_RATING_RETRIEVED",
                sellerRatingService.getSellerAverageRating(sellerId)));
    }

    /**
     * دریافت تعداد کل امتیازهای ثبت‌شده برای یک فروشنده مشخص.
     *
     * @param sellerId شناسه فروشنده مورد نظر
     * @return {@link ResponseEntity} حاوی تعداد امتیازهای این فروشنده
     */
    @GetMapping("/sellers/{sellerId}/count")
    public ResponseEntity<ApiResponse<Long>> getSellerRatingCount(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "RATING_COUNT_RETRIEVED",
                sellerRatingService.getSellerRatingCount(sellerId)));
    }
}
