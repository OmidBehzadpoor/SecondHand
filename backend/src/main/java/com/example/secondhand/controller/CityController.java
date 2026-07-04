package com.example.secondhand.controller;

import com.example.secondhand.dto.CityRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.CityResponse;
import com.example.secondhand.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cities", description = "مدیریت شهرها")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    @Operation(summary = "دریافت همه شهرها")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<CityResponse>>> getAllCities() {
        List<CityResponse> cities = cityService.getAllCities();
        ApiResponse<List<CityResponse>> response = new ApiResponse<>(
                true,
                "CITIES_RETRIEVED",
                cities
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "ایجاد شهر جدید - فقط ادمین")
    @PostMapping("")
    public ResponseEntity<ApiResponse<CityResponse>> createCity(@Valid @RequestBody CityRequest request) {
        CityResponse cityResponse = cityService.create(request);
        ApiResponse<CityResponse> response = new ApiResponse<>(true, "CITY_CREATED", cityResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "حذف شهر - فقط ادمین")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable Long id) {
        cityService.delete(id);
        ApiResponse<Void> response = new ApiResponse<>(true, "CITY_DELETED", null);
        return ResponseEntity.ok(response);
    }

}
