package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories() throws ApiException;
    List<CategoryResponse> getAllCategoriesForAdmin() throws ApiException;
    CategoryResponse createCategory(String name, Long parentId) throws ApiException;
    CategoryResponse updateCategory(Long id, String name, Long parentId) throws ApiException;
    CategoryResponse activateCategory(Long id) throws ApiException;
    CategoryResponse deactivateCategory(Long id) throws ApiException;
    void deleteCategory(Long id) throws ApiException;
}