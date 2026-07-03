package com.example.secondhand.controller;

import com.example.secondhand.dto.CityRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.CityResponse;
import com.example.secondhand.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

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
    @PostMapping("")
    public ResponseEntity<ApiResponse<CityResponse>> createCity(@Valid @RequestBody CityRequest request) {
        CityResponse cityResponse = cityService.create(request);
        ApiResponse<CityResponse> response = new ApiResponse<>(true, "CITY_CREATED", cityResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



}
