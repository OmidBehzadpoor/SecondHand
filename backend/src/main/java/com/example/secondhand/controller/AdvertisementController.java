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

/**
 * <h2>AdvertisementController</h2>
 * <p>
 * کنترلر اصلی مدیریت <b>آگهی‌ها</b>. این کنترلر تحت مسیر پایه {@code /api/advertisements}
 * قرار دارد. مشاهده لیست و جزئیات آگهی‌ها به‌صورت عمومی (بدون نیاز به توکن) در
 * دسترس است، در حالی که سایر عملیات (ایجاد، ویرایش، حذف، علامت‌گذاری به‌عنوان
 * فروخته‌شده و مشاهده‌ی آگهی‌های خود کاربر) نیاز به احراز هویت دارند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.AdvertisementService
 */
@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
@Validated
@Tag(name = "Advertisements", description = "مدیریت آگهی‌ها؛ مشاهده لیست و جزئیات عمومی است، بقیه عملیات نیاز به توکن دارد")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    /**
     * ایجاد یک آگهی جدید برای کاربر جاری.
     *
     * @param request     اطلاعات آگهی جدید
     * @param currentUser کاربر جاری (فروشنده‌ی آگهی)
     * @return {@link ResponseEntity} با کد وضعیت {@code 201 CREATED} و اطلاعات آگهی تازه‌ایجادشده
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AdvertisementResponse>> create(@Valid @RequestBody AdvertisementRequest request,
                                                                     @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "ADVERTISEMENT_CREATED", response));
    }

    /**
     * دریافت لیست تمام آگهی‌های ثبت‌شده توسط کاربر جاری.
     *
     * @param currentUser کاربر جاری
     * @return {@link ResponseEntity} حاوی لیست {@link AdvertisementResponse} متعلق به کاربر جاری
     */
    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<AdvertisementResponse>>> getMyAdvertisements(
            @AuthenticationPrincipal User currentUser) {
        List<AdvertisementResponse> responses = advertisementService.getMyAdvertisements(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "MY_ADVERTISEMENTS_RETRIEVED", responses));
    }

    /**
     * دریافت یک آگهی بر اساس شناسه.
     * <p>
     * این اندپوینت نیازی به توکن ندارد؛ در صورت ارسال توکن معتبر، سطح دسترسی
     * کاربر (مالک/ادمین/سایرین) در نمایش آگهی‌های غیرعمومی لحاظ می‌شود.
     * </p>
     *
     * @param id          شناسه آگهی مورد نظر
     * @param currentUser کاربر جاری (می‌تواند {@code null} باشد برای کاربر مهمان)
     * @return {@link ResponseEntity} حاوی {@link AdvertisementResponse} آگهی درخواستی
     */
    @GetMapping("/{id}")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AdvertisementResponse>> getById(@PathVariable Long id,
                                                                      @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.getById(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_RETRIEVED", response));
    }

    /**
     * جست‌وجو و دریافت لیست صفحه‌بندی‌شده‌ی آگهی‌های تاییدشده، با فیلترهای اختیاری.
     * <p>
     * این اندپوینت نیازی به توکن ندارد.
     * </p>
     *
     * @param keyword    کلمه کلیدی برای جست‌وجو (اختیاری)
     * @param categoryId شناسه دسته‌بندی برای فیلتر (اختیاری)
     * @param cityId     شناسه شهر برای فیلتر (اختیاری)
     * @param minPrice   حداقل قیمت مجاز؛ نباید منفی باشد (اختیاری)
     * @param maxPrice   حداکثر قیمت مجاز؛ نباید منفی باشد (اختیاری)
     * @param sortBy     نوع مرتب‌سازی نتایج (اختیاری)
     * @param pageable   اطلاعات صفحه‌بندی؛ به‌طور پیش‌فرض اندازه صفحه ۵۰ است
     * @return {@link ResponseEntity} حاوی صفحه‌ای از {@link AdvertisementResponse} مطابق با فیلترها
     */
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

    /**
     * ویرایش یک آگهی موجود توسط مالک آن.
     *
     * @param id          شناسه آگهی‌ای که باید ویرایش شود
     * @param request     اطلاعات جدید آگهی
     * @param currentUser کاربر جاری (باید مالک آگهی باشد)
     * @return {@link ResponseEntity} حاوی {@link AdvertisementResponse} به‌روزشده
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdvertisementResponse>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody AdvertisementRequest request,
                                                                     @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.update(id, request, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_UPDATED", response));
    }

    /**
     * حذف نرم (Soft Delete) یک آگهی توسط مالک آن.
     *
     * @param id          شناسه آگهی‌ای که باید حذف شود
     * @param currentUser کاربر جاری (باید مالک آگهی باشد)
     * @return {@link ResponseEntity} حاوی پیام موفقیت‌آمیز بودن عملیات حذف
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                    @AuthenticationPrincipal User currentUser) {
        advertisementService.delete(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_DELETED", null));
    }

    /**
     * علامت‌گذاری یک آگهی تاییدشده به‌عنوان فروخته‌شده، توسط مالک آن.
     *
     * @param id          شناسه آگهی‌ای که باید فروخته‌شده علامت زده شود
     * @param currentUser کاربر جاری (باید مالک آگهی باشد)
     * @return {@link ResponseEntity} حاوی {@link AdvertisementResponse} به‌روزشده پس از تغییر وضعیت
     */
    @PatchMapping("/{id}/sold")
    public ResponseEntity<ApiResponse<AdvertisementResponse>> markAsSold(@PathVariable Long id,
                                                                         @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.markAsSold(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_MARKED_AS_SOLD", response));
    }
}
