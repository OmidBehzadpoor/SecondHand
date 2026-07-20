package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.CityResponse;

import java.util.List;

public interface CityService {
    List<CityResponse> getAllCities() throws ApiException;
}