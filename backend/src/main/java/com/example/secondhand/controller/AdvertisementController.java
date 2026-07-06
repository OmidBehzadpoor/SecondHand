package com.example.secondhand.controller;

import com.example.secondhand.dto.AdvertisementRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.AdvertisementResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.service.AdvertisementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @PostMapping
    public ResponseEntity<ApiResponse<AdvertisementResponse>> create(@Valid @RequestBody AdvertisementRequest request,
                                                                     @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "ADVERTISEMENT_CREATED", response));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<AdvertisementResponse>> getMyAdvertisements(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(advertisementService.getMyAdvertisements(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> getById(@PathVariable Long id,
                                                         @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(advertisementService.getById(id, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<AdvertisementResponse>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long cityId) {
        return ResponseEntity.ok(advertisementService.getAll(categoryId, cityId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdvertisementResponse>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody AdvertisementRequest request,
                                                                     @AuthenticationPrincipal User currentUser) {
        AdvertisementResponse response = advertisementService.update(id, request, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_UPDATED", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id,
                                                    @AuthenticationPrincipal User currentUser) {
        advertisementService.delete(id, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "ADVERTISEMENT_DELETED", null));
    }
}