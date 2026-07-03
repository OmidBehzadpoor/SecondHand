package com.example.secondhand.controller;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.service.UserService;
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

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "EMAIL_VERIFIED", "ایمیل با موفقیت تایید شد"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @AuthenticationPrincipal User currentUser) {
        userService.resendVerificationEmail(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "EMAIL_SENT", "ایمیل تایید ارسال شد"));
    }
}