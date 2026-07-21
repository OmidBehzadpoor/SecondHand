package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories() throws ApiException;
    List<CategoryResponse> getAllCategoriesForAdmin() throws ApiException;
    CategoryResponse createCategory(String name, Long parentId) throws ApiException;
    void deleteCategory(Long id) throws ApiException;
}