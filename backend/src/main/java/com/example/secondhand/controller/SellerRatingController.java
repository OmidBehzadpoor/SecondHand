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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ratings")
@Tag(name = "Seller Ratings", description = "امتیازدهی به فروشنده")
public class SellerRatingController {

    private final SellerRatingService sellerRatingService;

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

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<ApiResponse<List<SellerRatingResponse>>> getSellerRatings(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "RATINGS_RETRIEVED",
                sellerRatingService.getSellerRatings(sellerId)));
    }

    @GetMapping("/sellers/{sellerId}/average")
    public ResponseEntity<ApiResponse<Double>> getSellerAverageRating(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "AVERAGE_RATING_RETRIEVED",
                sellerRatingService.getSellerAverageRating(sellerId)));
    }

    @GetMapping("/sellers/{sellerId}/count")
    public ResponseEntity<ApiResponse<Long>> getSellerRatingCount(
            @PathVariable Long sellerId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "RATING_COUNT_RETRIEVED",
                sellerRatingService.getSellerRatingCount(sellerId)));
    }
}