package com.example.secondhand.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUserAlreadyExists_shouldReturn409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUserAlreadyExists(new UserAlreadyExistsException("نام کاربری تکراری است"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("نام کاربری تکراری است", response.getBody().get("error"));
    }

    @Test
    void handleInvalidCredentials_shouldReturn401() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("نام کاربری یا رمز عبور اشتباه است"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleAdvertisementNotFound_shouldReturn404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAdvertisementNotFound(new AdvertisementNotFoundException("آگهی یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleCategoryNotFound_shouldReturn404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCategoryNotFound(new CategoryNotFoundException("دسته‌بندی یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleCityNotFound_shouldReturn404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCityNotFound(new CityNotFoundException("شهر یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleCategoryInUse_shouldReturn409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCategoryInUse(new CategoryInUseException("در حال استفاده است"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleCategoryHasChildren_shouldReturn409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCategoryHasChildren(new CategoryHasChildrenException("زیردسته دارد"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("زیردسته دارد", response.getBody().get("error"));
    }

    @Test
    void handleCityInUse_shouldReturn409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCityInUse(new CityInUseException("در حال استفاده است"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleUnauthorizedAction_shouldReturn403() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUnauthorizedAction(new UnauthorizedActionException("اجازه ندارید"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleInvalidAdvertisementState_shouldReturn400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidAdvertisementState(new InvalidAdvertisementStateException("وضعیت نامعتبر"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleInvalidCategoryHierarchy_shouldReturn400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidCategoryHierarchy(new InvalidCategoryHierarchyException("چرخه‌ی نامعتبر"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("چرخه‌ی نامعتبر", response.getBody().get("error"));
    }

    @Test
    void handleRatingAlreadyExists_shouldReturn409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleRatingAlreadyExists(new RatingAlreadyExistsException("قبلاً امتیاز داده‌اید"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleAccessDenied_shouldReturn403WithFixedMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("شما اجازه‌ی دسترسی به این عملیات را ندارید", response.getBody().get("error"));
    }

    @Test
    void handleFavoriteAlreadyExists_shouldReturn409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleFavoriteAlreadyExists(new FavoriteAlreadyExistsException("قبلاً اضافه شده"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleUserStateConflict_shouldReturn409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUserStateConflict(new UserStateConflictException("کاربر از قبل مسدود است"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("کاربر از قبل مسدود است", response.getBody().get("error"));
    }

    @Test
    void handleCategoryStateConflict_shouldReturn409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleCategoryStateConflict(new CategoryStateConflictException("دسته‌بندی از قبل فعال است"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("دسته‌بندی از قبل فعال است", response.getBody().get("error"));
    }

    @Test
    void handleFavoriteNotFound_shouldReturn404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleFavoriteNotFound(new FavoriteNotFoundException("علاقه‌مندی یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleConversationNotFound_shouldReturn404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleConversationNotFound(new ConversationNotFoundException("گفت‌وگو یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleUserNotFound_shouldReturn404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUserNotFound(new UserNotFoundException("کاربر یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("کاربر یافت نشد", response.getBody().get("error"));
    }

    @Test
    void handleUserBlocked_shouldReturn403() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUserBlocked(new UserBlockedException("حساب مسدود است"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnGenericMessage_whenExceptionMessageHasNoUniqueConstraintMarker() {
        ResponseEntity<Map<String, String>> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("constraint violated"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("این عملیات با داده‌ای که از قبل وجود دارد در تناقض است", response.getBody().get("error"));
    }

    @Test
    void handleDataIntegrityViolation_shouldReturnDuplicateMessage_whenExceptionMessageContainsUkMarker() {
        ResponseEntity<Map<String, String>> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("UK_USERNAME constraint failed"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("مقدار وارد شده تکراری است. لطفاً از مقدار دیگری استفاده کنید.", response.getBody().get("error"));
    }

    @Test
    void handleConstraintViolation_shouldReturn400WithJoinedMessages() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("قیمت نمی‌تواند منفی باشد");

        ConstraintViolationException ex =
                new ConstraintViolationException(Set.of(violation1));

        ResponseEntity<Map<String, String>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("قیمت نمی‌تواند منفی باشد", response.getBody().get("error"));
    }

    @Test
    void handleInvalidImage_shouldReturn400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidImage(new InvalidImageException("فرمت فایل مجاز نیست"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("فرمت فایل مجاز نیست", response.getBody().get("error"));
    }

    @Test
    void handleAdvertisementImageNotFound_shouldReturn404() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAdvertisementImageNotFound(new AdvertisementImageNotFoundException("تصویر یافت نشد"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("تصویر یافت نشد", response.getBody().get("error"));
    }

    @Test
    void handleMaxUploadSizeExceeded_shouldReturn400WithFixedMessage() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(5L * 1024 * 1024);

        ResponseEntity<Map<String, String>> response = handler.handleMaxUploadSizeExceeded(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("حجم فایل نباید بیشتر از ۵ مگابایت باشد", response.getBody().get("error"));
    }

    @Test
    void handleValidationErrors_shouldReturn400WithJoinedFieldErrorMessages() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "title", "عنوان نمی‌تواند خالی باشد"));
        bindingResult.addError(new FieldError("request", "price", "قیمت باید عدد مثبت باشد"));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("عنوان نمی‌تواند خالی باشد"));
        assertTrue(response.getBody().get("error").contains("قیمت باید عدد مثبت باشد"));
    }

    @Test
    void handleValidationErrors_shouldReturnFallbackMessage_whenNoFieldErrorsPresent() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("اطلاعات ارسالی معتبر نیست", response.getBody().get("error"));
    }
}
