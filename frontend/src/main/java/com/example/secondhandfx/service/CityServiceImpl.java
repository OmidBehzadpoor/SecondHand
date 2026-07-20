package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.model.CityResponse;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class CityServiceImpl implements CityService {

    @Override
    public List<CityResponse> getAllCities() throws ApiException {
        ApiResponse<List<CityResponse>> response = HttpClientHelper.get(
                "/api/cities",
                new TypeReference<ApiResponse<List<CityResponse>>>() {}
        );
        return response.getData();
    }
}