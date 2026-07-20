package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.model.CategoryResponse;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class CategoryServiceImpl implements CategoryService {

    @Override
    public List<CategoryResponse> getAllCategories() throws ApiException {
        ApiResponse<List<CategoryResponse>> response = HttpClientHelper.get(
                "/api/categories",
                new TypeReference<ApiResponse<List<CategoryResponse>>>() {}
        );
        return response.getData();
    }
}