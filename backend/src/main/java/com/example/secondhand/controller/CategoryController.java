package com.example.secondhand.controller;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.create(request);
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "CATEGORY_CREATED", categoryResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        ApiResponse<Void> response = new ApiResponse<>(true, "CATEGORY_DELETED", null);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.update(id, request);
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "CATEGORY_UPDATED", categoryResponse);
        return ResponseEntity.ok(response);
    }
}
