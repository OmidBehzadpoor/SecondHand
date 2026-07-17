package com.example.secondhand.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUserAlreadyExists_shouldReturn409WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUserAlreadyExists(new UserAlreadyExistsException("نام کاربری تکراری است"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("نام کاربری تکراری است", response.getBody().get("error"));
    }

    @Test
    void handleInvalidCredentials_shouldReturn401WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("نام کاربری یا رمز عبور نادرست است"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("نام کاربری یا رمز عبور نادرست است", response.getBody().get("error"));
    }

    @Test
    void handleAdvertisementNotFound_shouldReturn404WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAdvertisementNotFound(new AdvertisementNotFoundException("آگهی یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("آگهی یافت نشد", response.getBody().get("error"));
    }

    @Test
    void handleCategoryNotFound_shouldReturn404WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCategoryNotFound(new CategoryNotFoundException("دسته‌بندی یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("دسته‌بندی یافت نشد", response.getBody().get("error"));
    }

    @Test
    void handleCityNotFound_shouldReturn404WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCityNotFound(new CityNotFoundException("شهر یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("شهر یافت نشد", response.getBody().get("error"));
    }

    @Test
    void handleCategoryInUse_shouldReturn409WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCategoryInUse(new CategoryInUseException("این دسته‌بندی در حال استفاده است"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("این دسته‌بندی در حال استفاده است", response.getBody().get("error"));
    }

    @Test
    void handleCityInUse_shouldReturn409WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCityInUse(new CityInUseException("این شهر در حال استفاده است"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("این شهر در حال استفاده است", response.getBody().get("error"));
    }

    @Test
    void handleUnauthorizedAction_shouldReturn403WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUnauthorizedAction(new UnauthorizedActionException("اجازه‌ی این عملیات را ندارید"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("اجازه‌ی این عملیات را ندارید", response.getBody().get("error"));
    }

    @Test
    void handleInvalidAdvertisementState_shouldReturn400WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidAdvertisementState(new InvalidAdvertisementStateException("وضعیت آگهی نامعتبر است"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("وضعیت آگهی نامعتبر است", response.getBody().get("error"));
    }

    @Test
    void handleRatingAlreadyExists_shouldReturn409WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleRatingAlreadyExists(new RatingAlreadyExistsException("قبلاً امتیاز داده‌اید"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("قبلاً امتیاز داده‌اید", response.getBody().get("error"));
    }

    @Test
    void handleAccessDenied_shouldReturn403WithFixedMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("شما اجازه‌ی دسترسی به این عملیات را ندارید", response.getBody().get("error"));
    }

    @Test
    void handleFavoriteAlreadyExists_shouldReturn409WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleFavoriteAlreadyExists(new FavoriteAlreadyExistsException("قبلاً به علاقه‌مندی‌ها اضافه شده"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("قبلاً به علاقه‌مندی‌ها اضافه شده", response.getBody().get("error"));
    }

    @Test
    void handleFavoriteNotFound_shouldReturn404WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleFavoriteNotFound(new FavoriteNotFoundException("علاقه‌مندی یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("علاقه‌مندی یافت نشد", response.getBody().get("error"));
    }

    @Test
    void handleConversationNotFound_shouldReturn404WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleConversationNotFound(new ConversationNotFoundException("گفتگو یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("گفتگو یافت نشد", response.getBody().get("error"));
    }

    @Test
    void handleUserBlocked_shouldReturn403WithMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUserBlocked(new UserBlockedException("حساب کاربری مسدود است"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("حساب کاربری مسدود است", response.getBody().get("error"));
    }

    @Test
    void handleDataIntegrityViolation_shouldReturn409WithFixedMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("constraint violated"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("این عملیات با داده‌ای که از قبل وجود دارد در تناقض است", response.getBody().get("error"));
    }
}
