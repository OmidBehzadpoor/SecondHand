package com.example.secondhand.controller;

import com.example.secondhand.dto.response.AdvertisementImageResponse;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.exception.InvalidImageException;
import com.example.secondhand.service.AdvertisementImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <h2>AdvertisementImageController</h2>
 * <p>
 * کنترلر مسئول <b>آپلود و مدیریت تصاویر آگهی‌ها</b>. تمام اندپوینت‌های این
 * کنترلر تحت مسیر پایه {@code /api/advertisements/{advertisementId}/images}
 * قرار دارند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.AdvertisementImageService
 */
@RestController
@RequestMapping("/api/advertisements/{advertisementId}/images")
@RequiredArgsConstructor
@Tag(name = "Advertisement Images", description = "آپلود و مدیریت تصاویر آگهی")
public class AdvertisementImageController {

    private final AdvertisementImageService advertisementImageService;

    /**
     * دریافت لیست تمام تصاویر یک آگهی مشخص.
     *
     * @param advertisementId شناسه آگهی مورد نظر
     * @return {@link ResponseEntity} حاوی لیست {@link AdvertisementImageResponse} مرتبط با آگهی
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdvertisementImageResponse>>> list(
            @PathVariable Long advertisementId) {
        List<AdvertisementImageResponse> images = advertisementImageService.getImages(advertisementId);
        return ResponseEntity.ok(new ApiResponse<>(true, "IMAGES_RETRIEVED", images));
    }

    /**
     * آپلود یک تصویر جدید برای آگهی مشخص‌شده.
     *
     * @param advertisementId شناسه آگهی مقصد برای افزودن تصویر
     * @param file             فایل تصویر ارسالی به‌صورت {@code multipart/form-data}
     * @param currentUser      کاربر جاری (باید مالک آگهی باشد)
     * @return {@link ResponseEntity} با کد وضعیت {@code 201 CREATED} و اطلاعات تصویر ذخیره‌شده
     * @throws InvalidImageException در صورتی که فایل ارسالی خالی باشد
     */
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

    /**
     * حذف یک تصویر مشخص از یک آگهی.
     *
     * @param advertisementId شناسه آگهی‌ای که تصویر به آن تعلق دارد
     * @param imageId          شناسه تصویری که باید حذف شود
     * @param currentUser      کاربر جاری (باید مالک آگهی باشد)
     * @return {@link ResponseEntity} حاوی پیام موفقیت‌آمیز بودن عملیات حذف
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long advertisementId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal User currentUser) {
        advertisementImageService.deleteImage(advertisementId, imageId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "IMAGE_DELETED", null));
    }
}
