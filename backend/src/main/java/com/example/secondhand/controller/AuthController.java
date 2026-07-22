package com.example.secondhand.controller;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "ثبت‌نام و ورود؛ برای گرفتن توکن JWT جهت تست سایر اندپوینت‌ها از اینجا شروع کنید")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "ثبت‌نام کاربر جدید", description = "نیازی به توکن ندارد.")
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request);
        ApiResponse<Long> response = new ApiResponse<>(true, "REGISTER_SUCCESS", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "ورود و دریافت توکن JWT",
            description = "نیازی به توکن ندارد. مقدار \"token\" در پاسخ را کپی کرده و در دکمه Authorize وارد کنید تا بتوانید سایر اندپوینت‌ها را تست کنید.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "LOGIN_SUCCESS", response));
    }
}