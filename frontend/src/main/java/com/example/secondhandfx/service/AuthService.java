package com.example.secondhandfx.service;

import com.example.secondhandfx.model.LoginRequest;
import com.example.secondhandfx.model.LoginResponse;
import com.example.secondhandfx.model.RegisterRequest;
import com.example.secondhandfx.exception.ApiException;

public interface AuthService {

    LoginResponse login(LoginRequest request) throws ApiException;

    Long register(RegisterRequest request) throws ApiException;
}