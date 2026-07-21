package com.example.secondhand.controller;

import com.example.secondhand.dto.AdvertisementRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.AdvertisementResponse;
import com.example.secondhand.model.SortOption;
import com.example.secondhand.model.User;
import com.example.secondhand.service.AdvertisementService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
@Validated
@Tag(name = "Advertisements", description = "مدیریت آگهی‌ها؛ مشاهده لیست و جزئیات عمومی است، بقیه عملیات نیاز به توکن دارد")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @PostMapping
    public ResponseEntity<ApiResponse<AdvertisementResponse>> create(@Valid @RequestBody AdvertisementRequest request,
                                                                     @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "ADVERTISEMENT_CREATED", response));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<AdvertisementResponse>>> getMyAdvertisements(
            @AuthenticationPrincipal User currentUser) {
        List<AdvertisementResponse> responses = advertisementService.getMyAdvertisements(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "MY_ADVERTISEMENTS_RETRIEVED", responses));
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AdvertisementResponse>> getById(@PathVariable Long id,
                                                                      @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.getById(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_RETRIEVED", response));
    }

    @GetMapping
    @SecurityRequirements
    public ResponseEntity<ApiResponse<Page<AdvertisementResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) @Min(value = 0, message = "قیمت نمی‌تواند منفی باشد") Long minPrice,
            @RequestParam(required = false) @Min(value = 0, message = "قیمت نمی‌تواند منفی باشد") Long maxPrice,
            @RequestParam(required = false) SortOption sortBy,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<AdvertisementResponse> responses =
                advertisementService.getAll(keyword, categoryId, cityId, minPrice, maxPrice, sortBy, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENTS_RETRIEVED", responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdvertisementResponse>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody AdvertisementRequest request,
                                                                     @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.update(id, request, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_UPDATED", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                    @AuthenticationPrincipal User currentUser) {
        advertisementService.delete(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_DELETED", null));
    }

    @PatchMapping("/{id}/sold")
    public ResponseEntity<ApiResponse<AdvertisementResponse>> markAsSold(@PathVariable Long id,
                                                                         @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.markAsSold(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_MARKED_AS_SOLD", response));
    }
}