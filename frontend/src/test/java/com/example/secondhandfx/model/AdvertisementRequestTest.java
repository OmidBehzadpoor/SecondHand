package com.example.secondhandfx.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdvertisementRequestTest {

    @Test
    void builder_shouldSetAllProvidedFields() {
        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("گوشی سامسونگ")
                .description("در حد نو، بدون خط و خش")
                .price(15_000_000L)
                .categoryId(3L)
                .cityId(1L)
                .imageUrls(List.of("img1.jpg", "img2.jpg"))
                .build();

        assertEquals("گوشی سامسونگ", request.getTitle());
        assertEquals("در حد نو، بدون خط و خش", request.getDescription());
        assertEquals(15_000_000L, request.getPrice());
        assertEquals(3L, request.getCategoryId());
        assertEquals(1L, request.getCityId());
        assertEquals(2, request.getImageUrls().size());
    }

    @Test
    void builder_shouldAllowOmittingOptionalImageUrls() {
        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("میز تحریر")
                .description("چوبی، دست‌ساز")
                .price(2_000_000L)
                .categoryId(5L)
                .cityId(2L)
                .build();

        assertNull(request.getImageUrls());
    }

    @Test
    void advertisementRequest_shouldImplementApiRequestMarkerInterface() {
        AdvertisementRequest request = new AdvertisementRequest();

        assertTrue(request instanceof ApiRequest);
    }

    @Test
    void noArgsConstructorWithSetters_shouldProduceEquivalentObjectToBuilder() {
        AdvertisementRequest request = new AdvertisementRequest();
        request.setTitle("کتاب");
        request.setDescription("رمان");
        request.setPrice(50_000L);
        request.setCategoryId(7L);
        request.setCityId(4L);

        assertEquals("کتاب", request.getTitle());
        assertEquals(50_000L, request.getPrice());
        assertEquals(7L, request.getCategoryId());
    }
}
