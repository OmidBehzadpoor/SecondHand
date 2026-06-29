package com.example.secondhand.controller;

import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.model.User;
import com.example.secondhand.service.UserService;
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
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.ok("ثبت‌نام با موفقیت انجام شد. شناسه کاربر: " + user.getId());
    }
}