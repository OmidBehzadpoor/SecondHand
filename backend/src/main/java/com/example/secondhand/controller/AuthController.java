package com.example.secondhand.controller;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.service.PhoneVerificationService;
import com.example.secondhand.service.UserService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "🔐 Authentication", description = "ثبت‌نام، ورود و تایید هویت")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PhoneVerificationService phoneVerificationService;

    @Operation(
            summary = "ثبت‌نام کاربر جدید",
            description = "یک حساب کاربری جدید می‌سازد. بعد از ثبت‌نام ایمیل تایید ارسال می‌شود."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "ثبت‌نام موفق",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "messageCode": "REGISTER_SUCCESS",
                                      "data": 1
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "نام کاربری یا شماره تلفن تکراری",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "این نام کاربری قبلاً ثبت شده است"
                                    }""")))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "name": "امید بهزادپور",
                      "username": "omid",
                      "password": "mySecurePassword123",
                      "phone": "09123456789",
                      "email": "omid@example.com"
                    }""")))
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "REGISTER_SUCCESS", userId));
    }

    @Operation(
            summary = "ورود کاربر",
            description = "با نام کاربری و رمز عبور وارد شو و JWT بگیر. توکن را در Authorize استفاده کن."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "ورود موفق",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "messageCode": "LOGIN_SUCCESS",
                                      "data": {
                                        "token": "eyJhbGciOiJIUzI1NiJ9...",
                                        "userId": 1,
                                        "username": "omid",
                                        "role": "USER"
                                      }
                                    }"""))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "نام کاربری یا رمز عبور اشتباه",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "error": "نام کاربری یا رمز عبور اشتباه است"
                                    }""")))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "username": "omid",
                      "password": "mySecurePassword123"
                    }""")))
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "LOGIN_SUCCESS", response));
    }

    @Operation(
            summary = "تایید شماره تلفن",
            description = "کد ارسال شده از طریق تلگرام را وارد کن.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "تایید موفق"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "کد نامعتبر یا منقضی شده")
    })
    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<String>> verifyPhone(
            @RequestParam String code,
            @AuthenticationPrincipal User currentUser) {
        phoneVerificationService.verifyCode(currentUser, code);
        return ResponseEntity.ok(new ApiResponse<>(true, "PHONE_VERIFIED", "شماره تلفن با موفقیت تایید شد"));
    }

    @Operation(
            summary = "تایید ایمیل",
            description = "توکن ارسال شده به ایمیل را وارد کن."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "تایید موفق"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "توکن نامعتبر یا منقضی شده")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "EMAIL_VERIFIED", "ایمیل با موفقیت تایید شد"));
    }

    @Operation(
            summary = "ارسال مجدد ایمیل تایید",
            description = "اگر ایمیل تایید دریافت نکردی، دوباره ارسال کن.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "ایمیل ارسال شد")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @AuthenticationPrincipal User currentUser) {
        userService.resendVerificationEmail(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "EMAIL_SENT", "ایمیل تایید ارسال شد"));
    }
}