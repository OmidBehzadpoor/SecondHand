package com.example.secondhandfx.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionTest {

    @Test
    void constructor_shouldStoreMessageAndStatusCode() {
        ApiException exception = new ApiException("عملیات با خطا مواجه شد", 400);

        assertEquals("عملیات با خطا مواجه شد", exception.getMessage());
        assertEquals(400, exception.getStatusCode());
    }

    @Test
    void getStatusCode_shouldReturnZero_whenConstructedForNetworkError() {
        // طبق مستندات کلاس، کد وضعیت صفر یعنی خطای شبکه (بدون پاسخ از سرور)
        ApiException exception = new ApiException("امکان برقراری ارتباط با سرور وجود ندارد.", 0);

        assertEquals(0, exception.getStatusCode());
    }

    @Test
    void apiException_shouldBeACheckedException() {
        assertTrue(Exception.class.isAssignableFrom(ApiException.class));
        assertFalse(RuntimeException.class.isAssignableFrom(ApiException.class));
    }

    @Test
    void getStatusCode_shouldPreserveDifferentHttpStatusCodes() {
        assertEquals(404, new ApiException("یافت نشد", 404).getStatusCode());
        assertEquals(401, new ApiException("عدم دسترسی", 401).getStatusCode());
        assertEquals(500, new ApiException("خطای سرور", 500).getStatusCode());
    }

    @Test
    void thrownException_shouldBeCatchableAndCarryMessage() {
        ApiException caught = assertThrows(ApiException.class, () -> {
            throw new ApiException("خطای تستی", 409);
        });

        assertEquals("خطای تستی", caught.getMessage());
        assertEquals(409, caught.getStatusCode());
    }
}
