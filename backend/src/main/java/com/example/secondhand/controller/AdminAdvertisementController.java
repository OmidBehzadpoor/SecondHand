package com.example.secondhand.controller;

import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.AdminRejectRequest;
import com.example.secondhand.dto.response.AdminAdvertisementResponse;
import com.example.secondhand.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/advertisements")
@RequiredArgsConstructor
public class AdminAdvertisementController {

    private final AdvertisementService advertisementService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminAdvertisementResponse>>> getPending() {
        List<AdminAdvertisementResponse> responses = advertisementService.getPendingAdvertisements();
        return ResponseEntity.ok(new ApiResponse<>(true, "PENDING_ADVERTISEMENTS_RETRIEVED", responses));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminAdvertisementResponse>> approve(@PathVariable Long id) {
        AdminAdvertisementResponse response = advertisementService.approve(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_APPROVED", response));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminAdvertisementResponse>> reject(
            @PathVariable Long id, @Valid @RequestBody AdminRejectRequest request) {
        AdminAdvertisementResponse response = advertisementService.reject(id, request.getReason());
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_REJECTED", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        advertisementService.adminDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_DELETED_BY_ADMIN", null));
    }
}