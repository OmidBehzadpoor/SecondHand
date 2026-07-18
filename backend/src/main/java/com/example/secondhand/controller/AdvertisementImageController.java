package com.example.secondhand.controller;

import com.example.secondhand.dto.response.AdvertisementImageResponse;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.exception.InvalidImageException;
import com.example.secondhand.service.AdvertisementImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/advertisements/{advertisementId}/images")
@RequiredArgsConstructor
public class AdvertisementImageController {

    private final AdvertisementImageService advertisementImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AdvertisementImageResponse>> upload(
            @PathVariable Long advertisementId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {
        if (file.isEmpty()) {
            throw new InvalidImageException("فایل تصویر ارسال نشده است");
        }
        AdvertisementImageResponse response = advertisementImageService.uploadImage(advertisementId, file, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "IMAGE_UPLOADED", response));
    }
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long advertisementId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal User currentUser) {
        advertisementImageService.deleteImage(advertisementId, imageId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "IMAGE_DELETED", null));
    }
}