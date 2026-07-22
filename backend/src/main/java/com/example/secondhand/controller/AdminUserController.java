package com.example.secondhand.controller;

import com.example.secondhand.dto.response.AdminUserResponse;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "مدیریت کاربران توسط مدیر - نیاز به توکن مدیر")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAllUsers() {
        List<AdminUserResponse> users = userService.getAllUsersForAdmin();
        return ResponseEntity.ok(new ApiResponse<>(true, "USERS_FETCHED", users));
    }

    @PatchMapping("/{userId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserResponse>> blockUser(@PathVariable Long userId) {
        AdminUserResponse response = userService.blockUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "USER_BLOCKED", response));
    }

    @PatchMapping("/{userId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserResponse>> unblockUser(@PathVariable Long userId) {
        AdminUserResponse response = userService.unblockUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "USER_UNBLOCKED", response));
    }
}