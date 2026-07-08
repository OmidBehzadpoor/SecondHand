package com.example.secondhand.controller;

import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.AdvertisementResponse;
import com.example.secondhand.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/advertisements")
@RequiredArgsConstructor
public class AdminAdvertisementController {

    private final AdvertisementService advertisementService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdvertisementResponse>>> getPending() {
        List<AdvertisementResponse> responses = advertisementService.getPendingAdvertisements();
        return ResponseEntity.ok(new ApiResponse<>(true, "PENDING_ADVERTISEMENTS_RETRIEVED", responses));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdvertisementResponse>> approve(@PathVariable Long id) {
        AdvertisementResponse response = advertisementService.approve(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_APPROVED", response));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdvertisementResponse>> reject(@PathVariable Long id) {
        AdvertisementResponse response = advertisementService.reject(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_REJECTED", response));
    }
}