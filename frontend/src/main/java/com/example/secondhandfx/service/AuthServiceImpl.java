package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.model.LoginRequest;
import com.example.secondhandfx.model.LoginResponse;
import com.example.secondhandfx.model.RegisterRequest;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;

public class AuthServiceImpl implements AuthService {

    @Override
    public LoginResponse login(LoginRequest request) throws ApiException {
        return HttpClientHelper.post("/api/auth/login", request,
                new TypeReference<ApiResponse<LoginResponse>>() {}).getData();
    }

    @Override
    public Long register(RegisterRequest request) throws ApiException {
        return HttpClientHelper.post("/api/auth/register", request,
                new TypeReference<ApiResponse<Long>>() {}).getData();
    }
}