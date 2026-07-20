package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.CityResponse;

import java.util.List;

public interface CityService {
    List<CityResponse> getAllCities() throws ApiException;
    CityResponse createCity(String name) throws ApiException;
    void deleteCity(Long id) throws ApiException;
}