package com.example.secondhandfx.util;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    // NOTE: config/config.properties ممکن است در سیستم‌های مختلف وجود داشته
    // یا نداشته باشد (و مقدار متفاوتی override کند)، پس این تست‌ها فقط روی
    // قراردادی که مستقل از محیط اجراست تمرکز دارند، نه مقدار دقیق URL.

    @Test
    void getApiBaseUrl_shouldNeverReturnNullOrBlank() {
        String url = Config.getApiBaseUrl();

        assertNotNull(url);
        assertFalse(url.isBlank());
    }

    @Test
    void getApiBaseUrl_shouldReturnTheSameValueOnRepeatedCalls() {
        String first = Config.getApiBaseUrl();
        String second = Config.getApiBaseUrl();

        assertEquals(first, second);
        assertSame(first, second);
    }

    @Test
    void getApiBaseUrl_shouldBeAValidUri() {
        assertDoesNotThrow(() -> URI.create(Config.getApiBaseUrl()));
    }

    @Test
    void getApiBaseUrl_shouldNotContainWhitespaceOrTrailingSlash() {
        String url = Config.getApiBaseUrl();

        assertFalse(url.contains(" "));
        assertFalse(url.endsWith("/"));
    }
}