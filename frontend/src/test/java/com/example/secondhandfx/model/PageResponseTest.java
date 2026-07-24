package com.example.secondhandfx.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void allArgsConstructor_shouldSetEveryField() {
        List<String> content = List.of("ad-1", "ad-2");

        PageResponse<String> page = new PageResponse<>(content, 5, 42L, 0, 10, false);

        assertEquals(content, page.getContent());
        assertEquals(5, page.getTotalPages());
        assertEquals(42L, page.getTotalElements());
        assertEquals(0, page.getNumber());
        assertEquals(10, page.getSize());
        assertFalse(page.isLast());
    }

    @Test
    void noArgsConstructorWithSetters_shouldPopulateEquivalentInstance() {
        PageResponse<String> page = new PageResponse<>();
        page.setContent(List.of("ad-1"));
        page.setTotalPages(1);
        page.setTotalElements(1L);
        page.setNumber(0);
        page.setSize(10);
        page.setLast(true);

        assertEquals(List.of("ad-1"), page.getContent());
        assertTrue(page.isLast());
    }

    @Test
    void isLast_shouldDefaultToFalse_whenNotExplicitlySet() {
        PageResponse<String> page = new PageResponse<>();

        assertFalse(page.isLast());
    }

    @Test
    void content_shouldSupportAnEmptyList_forPagesWithNoResults() {
        PageResponse<String> page = new PageResponse<>(List.of(), 0, 0L, 0, 10, true);

        assertNotNull(page.getContent());
        assertTrue(page.getContent().isEmpty());
        assertTrue(page.isLast());
    }
}
