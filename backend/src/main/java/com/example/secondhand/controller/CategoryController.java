package com.example.secondhand.controller;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h2>CategoryController</h2>
 * <p>
 * کنترلر مدیریت <b>دسته‌بندی‌های</b> سلسله‌مراتبی آگهی‌ها. تحت مسیر پایه
 * {@code /api/categories} قرار دارد. مشاهده‌ی درخت دسته‌بندی‌های فعال برای
 * همه در دسترس است؛ سایر عملیات (ایجاد، ویرایش، حذف، فعال/غیرفعال‌سازی و
 * مشاهده‌ی همه‌ی دسته‌بندی‌ها) مخصوص کاربران با نقش {@code ADMIN} است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.CategoryService
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "دسته‌بندی آگهی‌ها")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * دریافت درخت دسته‌بندی‌های <b>فعال</b>، مخصوص نمایش عمومی.
     *
     * @return {@link ResponseEntity} حاوی لیست {@link CategoryResponse} به‌همراه زیردسته‌های فعال
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        ApiResponse<List<CategoryResponse>> response = new ApiResponse<>(
                true,
                "CATEGORIES_RETRIEVED",
                categories
        );
        return ResponseEntity.ok(response);
    }

    /**
     * ایجاد یک دسته‌بندی جدید، توسط ادمین.
     *
     * @param request اطلاعات دسته‌بندی جدید شامل نام و شناسه والد (اختیاری)
     * @return {@link ResponseEntity} با کد وضعیت {@code 201 CREATED} و اطلاعات دسته‌بندی تازه‌ایجادشده
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.create(request);
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "CATEGORY_CREATED", categoryResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * حذف یک دسته‌بندی، توسط ادمین.
     *
     * @param id شناسه دسته‌بندی‌ای که باید حذف شود
     * @return {@link ResponseEntity} حاوی پیام موفقیت‌آمیز بودن عملیات حذف
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        ApiResponse<Void> response = new ApiResponse<>(true, "CATEGORY_DELETED", null);
        return ResponseEntity.ok(response);
    }

    /**
     * ویرایش نام و/یا والد یک دسته‌بندی موجود، توسط ادمین.
     *
     * @param id      شناسه دسته‌بندی‌ای که باید ویرایش شود
     * @param request اطلاعات جدید دسته‌بندی
     * @return {@link ResponseEntity} حاوی {@link CategoryResponse} به‌روزشده
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.update(id, request);
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "CATEGORY_UPDATED", categoryResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * دریافت درخت تمام دسته‌بندی‌ها (فعال و غیرفعال)، مخصوص پنل ادمین.
     *
     * @return {@link ResponseEntity} حاوی لیست کامل {@link CategoryResponse}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategoriesForAdmin() {
        List<CategoryResponse> categories = categoryService.getAllCategoriesForAdmin();
        ApiResponse<List<CategoryResponse>> response = new ApiResponse<>(
                true, "CATEGORIES_RETRIEVED", categories
        );
        return ResponseEntity.ok(response);
    }

    /**
     * فعال‌سازی یک دسته‌بندی غیرفعال، توسط ادمین.
     *
     * @param id شناسه دسته‌بندی‌ای که باید فعال شود
     * @return {@link ResponseEntity} حاوی {@link CategoryResponse} به‌روزشده پس از فعال‌سازی
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<CategoryResponse>> activateCategory(@PathVariable Long id) {
        CategoryResponse categoryResponse = categoryService.activate(id);
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "CATEGORY_ACTIVATED", categoryResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * غیرفعال‌سازی یک دسته‌بندی فعال، توسط ادمین.
     *
     * @param id شناسه دسته‌بندی‌ای که باید غیرفعال شود
     * @return {@link ResponseEntity} حاوی {@link CategoryResponse} به‌روزشده پس از غیرفعال‌سازی
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CategoryResponse>> deactivateCategory(@PathVariable Long id) {
        CategoryResponse categoryResponse = categoryService.deactivate(id);
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "CATEGORY_DEACTIVATED", categoryResponse);
        return ResponseEntity.ok(response);
    }
}
