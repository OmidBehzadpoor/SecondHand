package com.example.secondhand.controller;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <h2>AuthController</h2>
 * <p>
 * کنترلر مربوط به احراز هویت کاربران، شامل عملیات <b>ثبت‌نام</b> و <b>ورود</b>.
 * این کنترلر تحت مسیر پایه {@code /api/auth} در دسترس است و نیازی به توکن JWT
 * برای دسترسی به اندپوینت‌های آن نیست (با {@code @SecurityRequirements} مشخص شده).
 * </p>
 * <p>
 * معمولاً برای تست سایر اندپوینت‌های سامانه، ابتدا باید از طریق متد {@link #login}
 * توکن دریافت شود و سپس در Swagger با دکمه <i>Authorize</i> استفاده شود.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.UserService
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "ثبت‌نام و ورود؛ برای گرفتن توکن JWT جهت تست سایر اندپوینت‌ها از اینجا شروع کنید")
public class AuthController {

    private final UserService userService;

    /**
     * ثبت‌نام یک کاربر جدید در سامانه.
     * <p>
     * این اندپوینت نیازی به توکن ندارد و اطلاعات ورودی با استفاده از
     * {@code @Valid} اعتبارسنجی می‌شوند.
     * </p>
     *
     * @param request اطلاعات ثبت‌نام کاربر (نام، نام کاربری، رمز عبور، شماره تماس، ایمیل)
     * @return {@link ResponseEntity} با کد وضعیت {@code 201 CREATED} و شناسه کاربر تازه ثبت‌شده
     *         در قالب {@link ApiResponse}
     */
    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "ثبت‌نام کاربر جدید", description = "نیازی به توکن ندارد.")
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = userService.register(request);
        ApiResponse<Long> response = new ApiResponse<>(true, "REGISTER_SUCCESS", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ورود کاربر به سامانه و دریافت توکن JWT.
     * <p>
     * این اندپوینت نیازی به توکن ندارد. مقدار {@code token} موجود در پاسخ باید
     * برای فراخوانی اندپوینت‌های محافظت‌شده در هدر {@code Authorization} استفاده شود.
     * </p>
     *
     * @param request اطلاعات ورود شامل نام کاربری و رمز عبور
     * @return {@link ResponseEntity} با کد وضعیت {@code 200 OK} و اطلاعات ورود
     *         ({@link LoginResponse}) شامل توکن JWT، در قالب {@link ApiResponse}
     */
    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "ورود و دریافت توکن JWT",
            description = "نیازی به توکن ندارد. مقدار \"token\" در پاسخ را کپی کرده و در دکمه Authorize وارد کنید تا بتوانید سایر اندپوینت‌ها را تست کنید.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "LOGIN_SUCCESS", response));
    }
}
