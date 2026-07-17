package com.example.secondhand.service;

import com.example.secondhand.dto.SellerRatingRequest;
import com.example.secondhand.dto.response.SellerRatingResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.RatingAlreadyExistsException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.*;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.SellerRatingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerRatingServiceTest {

    @Mock
    private SellerRatingRepository sellerRatingRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private SellerRatingService sellerRatingService;

    // ==================== rateAdvertisement ====================

    @Test
    void rateAdvertisement_shouldSaveRating_whenValid() {
        User seller = User.builder().id(1L).username("seller").build();
        User buyer = User.builder().id(2L).username("buyer").build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .title("Laptop")
                .status(AdvertisementStatus.APPROVED)
                .seller(seller)
                .build();

        SellerRatingRequest request = SellerRatingRequest.builder()
                .rating(5)
                .comment("عالی بود")
                .build();

        SellerRating savedRating = SellerRating.builder()
                .id(1L)
                .buyer(buyer)
                .advertisement(ad)
                .rating(5)
                .comment("عالی بود")
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(sellerRatingRepository.existsByBuyerIdAndAdvertisementId(2L, 10L)).thenReturn(false);
        when(sellerRatingRepository.save(any(SellerRating.class))).thenReturn(savedRating);

        SellerRatingResponse response = sellerRatingService.rateAdvertisement(10L, request, buyer);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("عالی بود", response.getComment());
        assertEquals(2L, response.getBuyerId());
        verify(sellerRatingRepository, times(1)).save(any(SellerRating.class));
    }

    @Test
    void rateAdvertisement_shouldSaveRating_whenAdvertisementIsSold() {
        User seller = User.builder().id(1L).username("seller").build();
        User buyer = User.builder().id(2L).username("buyer").build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.SOLD)
                .seller(seller)
                .build();

        SellerRatingRequest request = SellerRatingRequest.builder()
                .rating(4)
                .comment("خوب بود")
                .build();

        SellerRating savedRating = SellerRating.builder()
                .id(1L)
                .buyer(buyer)
                .advertisement(ad)
                .rating(4)
                .comment("خوب بود")
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(sellerRatingRepository.existsByBuyerIdAndAdvertisementId(2L, 10L)).thenReturn(false);
        when(sellerRatingRepository.save(any(SellerRating.class))).thenReturn(savedRating);

        SellerRatingResponse response = sellerRatingService.rateAdvertisement(10L, request, buyer);

        assertNotNull(response);
        assertEquals(4, response.getRating());
        verify(sellerRatingRepository, times(1)).save(any(SellerRating.class));
    }

    @Test
    void rateAdvertisement_shouldThrowAdvertisementNotFoundException_whenAdDoesNotExist() {
        User buyer = User.builder().id(2L).build();
        SellerRatingRequest request = SellerRatingRequest.builder().rating(5).build();

        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> sellerRatingService.rateAdvertisement(99L, request, buyer));

        verify(sellerRatingRepository, never()).save(any());
    }

    @Test
    void rateAdvertisement_shouldThrowAdvertisementNotFoundException_whenAdIsPending() {
        User buyer = User.builder().id(2L).build();
        User seller = User.builder().id(1L).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.PENDING)
                .seller(seller)
                .build();

        SellerRatingRequest request = SellerRatingRequest.builder().rating(5).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> sellerRatingService.rateAdvertisement(10L, request, buyer));

        verify(sellerRatingRepository, never()).save(any());
    }

    @Test
    void rateAdvertisement_shouldThrowUnauthorizedActionException_whenBuyerIsSeller() {
        User seller = User.builder().id(1L).username("seller").build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.APPROVED)
                .seller(seller)
                .build();

        SellerRatingRequest request = SellerRatingRequest.builder().rating(5).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> sellerRatingService.rateAdvertisement(10L, request, seller));

        verify(sellerRatingRepository, never()).save(any());
    }

    @Test
    void rateAdvertisement_shouldThrowRatingAlreadyExistsException_whenAlreadyRated() {
        User seller = User.builder().id(1L).build();
        User buyer = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.APPROVED)
                .seller(seller)
                .build();

        SellerRatingRequest request = SellerRatingRequest.builder().rating(3).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(sellerRatingRepository.existsByBuyerIdAndAdvertisementId(2L, 10L)).thenReturn(true);

        assertThrows(RatingAlreadyExistsException.class,
                () -> sellerRatingService.rateAdvertisement(10L, request, buyer));

        verify(sellerRatingRepository, never()).save(any());
    }

    // ==================== getSellerRatings ====================

    @Test
    void getSellerRatings_shouldReturnRatings_whenSellerHasSome() {
        User seller = User.builder().id(1L).username("seller").build();
        User buyer = User.builder().id(2L).username("buyer").build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .title("Laptop")
                .seller(seller)
                .build();

        SellerRating rating = SellerRating.builder()
                .id(1L)
                .buyer(buyer)
                .advertisement(ad)
                .rating(5)
                .comment("عالی")
                .build();

        when(sellerRatingRepository.findByAdvertisementSellerId(1L)).thenReturn(List.of(rating));

        List<SellerRatingResponse> responses = sellerRatingService.getSellerRatings(1L);

        assertEquals(1, responses.size());
        assertEquals(5, responses.get(0).getRating());
        assertEquals("عالی", responses.get(0).getComment());
    }

    @Test
    void getSellerRatings_shouldReturnEmptyList_whenSellerHasNone() {
        when(sellerRatingRepository.findByAdvertisementSellerId(1L)).thenReturn(List.of());

        List<SellerRatingResponse> responses = sellerRatingService.getSellerRatings(1L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // ==================== getSellerAverageRating ====================

    @Test
    void getSellerAverageRating_shouldReturnAverage_whenRatingsExist() {
        User seller = User.builder().id(1L).build();
        User buyer1 = User.builder().id(2L).username("buyer1").build();
        User buyer2 = User.builder().id(3L).username("buyer2").build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .title("Laptop")
                .seller(seller)
                .build();

        SellerRating rating1 = SellerRating.builder()
                .id(1L).buyer(buyer1).advertisement(ad).rating(4).build();
        SellerRating rating2 = SellerRating.builder()
                .id(2L).buyer(buyer2).advertisement(ad).rating(2).build();

        when(sellerRatingRepository.findByAdvertisementSellerId(1L))
                .thenReturn(List.of(rating1, rating2));

        Double average = sellerRatingService.getSellerAverageRating(1L);

        assertEquals(3.0, average);
    }

    @Test
    void getSellerAverageRating_shouldReturnZero_whenNoRatingsExist() {
        when(sellerRatingRepository.findByAdvertisementSellerId(1L)).thenReturn(List.of());

        Double average = sellerRatingService.getSellerAverageRating(1L);

        assertEquals(0.0, average);
    }

    // ==================== getSellerRatingCount ====================

    @Test
    void getSellerRatingCount_shouldReturnCount_whenRatingsExist() {
        User seller = User.builder().id(1L).build();
        User buyer = User.builder().id(2L).username("buyer").build();

        Advertisement ad = Advertisement.builder()
                .id(10L).title("Laptop").seller(seller).build();

        SellerRating rating = SellerRating.builder()
                .id(1L).buyer(buyer).advertisement(ad).rating(5).build();

        when(sellerRatingRepository.findByAdvertisementSellerId(1L)).thenReturn(List.of(rating));

        Long count = sellerRatingService.getSellerRatingCount(1L);

        assertEquals(1L, count);
    }

    @Test
    void getSellerRatingCount_shouldReturnZero_whenNoRatingsExist() {
        when(sellerRatingRepository.findByAdvertisementSellerId(1L)).thenReturn(List.of());

        Long count = sellerRatingService.getSellerRatingCount(1L);

        assertEquals(0L, count);
    }
}