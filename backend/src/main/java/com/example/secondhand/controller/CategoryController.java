package com.example.secondhand.controller;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse categoryResponse = categoryService.create(request);
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "CATEGORY_CREATED", categoryResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
