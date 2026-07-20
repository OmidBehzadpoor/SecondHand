package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.model.CategoryRequest;
import com.example.secondhandfx.model.CategoryResponse;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class CategoryServiceImpl implements CategoryService {

    @Override
    public List<CategoryResponse> getAllCategories() throws ApiException {
        return HttpClientHelper.get(
                "/api/categories",
                new TypeReference<ApiResponse<List<CategoryResponse>>>() {}
        ).getData();
    }

    @Override
    public CategoryResponse createCategory(String name) throws ApiException {
        CategoryRequest request = CategoryRequest.builder().name(name).build();
        return HttpClientHelper.post(
                "/api/categories",
                request,
                new TypeReference<ApiResponse<CategoryResponse>>() {}
        ).getData();
    }

    @Override
    public void deleteCategory(Long id) throws ApiException {
        HttpClientHelper.delete(
                "/api/categories/" + id,
                new TypeReference<ApiResponse<Void>>() {}
        );
    }
}