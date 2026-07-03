package com.example.secondhand.controller;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.service.UserService;
import com.example.secondhand.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PhoneVerificationService phoneVerificationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request);
        ApiResponse<Long> response = new ApiResponse<>(true, "REGISTER_SUCCESS", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "LOGIN_SUCCESS", response));
    }

    @PostMapping("/send-phone-verification")
    public ResponseEntity<ApiResponse<String>> sendPhoneVerification(
            @RequestParam String telegramChatId,
            @AuthenticationPrincipal User currentUser) {
        phoneVerificationService.sendVerificationCode(currentUser, telegramChatId);
        return ResponseEntity.ok(new ApiResponse<>(true, "CODE_SENT", "کد تایید ارسال شد"));
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<String>> verifyPhone(
            @RequestParam String code,
            @AuthenticationPrincipal User currentUser) {
        phoneVerificationService.verifyCode(currentUser, code);
        return ResponseEntity.ok(new ApiResponse<>(true, "PHONE_VERIFIED", "شماره تلفن با موفقیت تایید شد"));
    }
}