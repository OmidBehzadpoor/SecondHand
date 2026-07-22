package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.model.CityRequest;
import com.example.secondhandfx.model.CityResponse;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class CityServiceImpl implements CityService {

    @Override
    public List<CityResponse> getAllCities() throws ApiException {
        return HttpClientHelper.get(
                "/api/cities",
                new TypeReference<ApiResponse<List<CityResponse>>>() {}
        ).getData();
    }

    @Override
    public CityResponse createCity(String name) throws ApiException {
        CityRequest request = CityRequest.builder().name(name).build();
        return HttpClientHelper.post(
                "/api/cities",
                request,
                new TypeReference<ApiResponse<CityResponse>>() {}
        ).getData();
    }

    @Override
    public void deleteCity(Long id) throws ApiException {
        HttpClientHelper.delete(
                "/api/cities/" + id,
                new TypeReference<ApiResponse<Void>>() {}
        );
    }
}