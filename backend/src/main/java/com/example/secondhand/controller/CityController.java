package com.example.secondhand.controller;

import com.example.secondhand.dto.CityRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.CityResponse;
import com.example.secondhand.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "🏙️ Cities", description = "مدیریت شهرها")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cities")
public class CityController {

    private final CityService cityService;

    @Operation(summary = "دریافت همه شهرها", description = "عمومی — نیاز به توکن ندارد")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "لیست شهرها",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "messageCode": "CITIES_RETRIEVED",
                              "data": [
                                {"id": 1, "name": "تهران"},
                                {"id": 2, "name": "اصفهان"},
                                {"id": 3, "name": "شیراز"}
                              ]
                            }""")))
    @GetMapping
    public ResponseEntity<ApiResponse<List<CityResponse>>> getAllCities() {
        return ResponseEntity.ok(new ApiResponse<>(true, "CITIES_RETRIEVED", cityService.getAllCities()));
    }

    @Operation(summary = "ایجاد شهر جدید", description = "فقط ADMIN",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "شهر ساخته شد",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "messageCode": "CITY_CREATED",
                                      "data": {"id": 4, "name": "مشهد"}
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "دسترسی ممنوع",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "دسترسی ممنوع است"
                                    }""")))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = """
                    {"name": "مشهد"}""")))
    @PostMapping
    public ResponseEntity<ApiResponse<CityResponse>> createCity(@Valid @RequestBody CityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "CITY_CREATED", cityService.create(request)));
    }

    @Operation(summary = "حذف شهر", description = "فقط ADMIN",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "حذف موفق",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "messageCode": "CITY_DELETED",
                                      "data": null
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "شهر یافت نشد",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "شهر یافت نشد"
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "دسترسی ممنوع",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "دسترسی ممنوع است"
                                    }""")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable Long id) {
        cityService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "CITY_DELETED", null));
    }
}