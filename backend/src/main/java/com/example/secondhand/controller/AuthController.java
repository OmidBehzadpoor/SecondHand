package com.example.secondhand.controller;

import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request);
        ApiResponse response = new ApiResponse(true, "REGISTER_SUCCESS", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}