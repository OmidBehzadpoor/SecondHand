package com.example.secondhand.service;

import com.example.secondhand.dto.SellerRatingRequest;
import com.example.secondhand.dto.SellerRatingSummary;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerRatingServiceTest {

    @Mock
    private SellerRatingRepository sellerRatingRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private SellerRatingService sellerRatingService;

    private User buyer() {
        return User.builder().id(2L).build();
    }

    private User seller() {
        return User.builder().id(1L).build();
    }

    private SellerRatingRequest request(Integer rating) {
        SellerRatingRequest request = new SellerRatingRequest();
        request.setRating(rating);
        request.setComment("خوب بود");
        return request;
    }

    // ==================== rateAdvertisement ====================

    @Test
    void rateAdvertisement_shouldSaveRating_whenAdvertisementIsApproved() {
        User buyer = buyer();
        User seller = seller();
        Advertisement ad = Advertisement.builder()
                .id(10L).title("Laptop").status(AdvertisementStatus.APPROVED).seller(seller).build();

        SellerRating saved = SellerRating.builder()
                .id(1L).buyer(buyer).advertisement(ad).rating(5).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(sellerRatingRepository.existsByBuyerIdAndAdvertisementId(2L, 10L)).thenReturn(false);
        when(sellerRatingRepository.save(any(SellerRating.class))).thenReturn(saved);

        SellerRatingResponse response = sellerRatingService.rateAdvertisement(10L, request(5), buyer);

        assertEquals(5, response.getRating());
    }

    @Test
    void rateAdvertisement_shouldSaveRating_whenAdvertisementIsSold() {
        User buyer = buyer();
        User seller = seller();
        Advertisement ad = Advertisement.builder()
                .id(10L).title("Laptop").status(AdvertisementStatus.SOLD).seller(seller).build();

        SellerRating saved = SellerRating.builder().id(1L).buyer(buyer).advertisement(ad).rating(4).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(sellerRatingRepository.existsByBuyerIdAndAdvertisementId(2L, 10L)).thenReturn(false);
        when(sellerRatingRepository.save(any(SellerRating.class))).thenReturn(saved);

        SellerRatingResponse response = sellerRatingService.rateAdvertisement(10L, request(4), buyer);

        assertEquals(4, response.getRating());
    }

    @Test
    void rateAdvertisement_shouldThrowAdvertisementNotFoundException_whenPending() {
        User seller = seller();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.PENDING).seller(seller).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> sellerRatingService.rateAdvertisement(10L, request(5), buyer()));

        verify(sellerRatingRepository, never()).save(any());
    }

    @Test
    void rateAdvertisement_shouldThrowAdvertisementNotFoundException_whenRejected() {
        User seller = seller();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.REJECTED).seller(seller).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> sellerRatingService.rateAdvertisement(10L, request(5), buyer()));
    }

    @Test
    void rateAdvertisement_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {
        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> sellerRatingService.rateAdvertisement(99L, request(5), buyer()));
    }

    @Test
    void rateAdvertisement_shouldThrowUnauthorizedActionException_whenRatingOwnAdvertisement() {
        User seller = seller();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(seller).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> sellerRatingService.rateAdvertisement(10L, request(5), seller));

        verify(sellerRatingRepository, never()).save(any());
    }

    @Test
    void rateAdvertisement_shouldThrowRatingAlreadyExistsException_whenAlreadyRated() {
        User buyer = buyer();
        User seller = seller();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(seller).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(sellerRatingRepository.existsByBuyerIdAndAdvertisementId(2L, 10L)).thenReturn(true);

        assertThrows(RatingAlreadyExistsException.class,
                () -> sellerRatingService.rateAdvertisement(10L, request(5), buyer));

        verify(sellerRatingRepository, never()).save(any());
    }

    // ==================== getSellerRatings ====================

    @Test
    void getSellerRatings_shouldReturnAllRatingsForSeller() {
        User buyer = buyer();
        User seller = seller();
        Advertisement ad = Advertisement.builder().id(10L).title("Laptop").seller(seller).build();
        SellerRating rating = SellerRating.builder().id(1L).buyer(buyer).advertisement(ad).rating(5).build();

        when(sellerRatingRepository.findByAdvertisementSellerId(1L)).thenReturn(List.of(rating));

        List<SellerRatingResponse> result = sellerRatingService.getSellerRatings(1L);

        assertEquals(1, result.size());
    }

    // ==================== getSellerRatingCount ====================

    @Test
    void getSellerRatingCount_shouldReturnCorrectCount() {
        User buyer = buyer();
        User seller = seller();
        Advertisement ad = Advertisement.builder().id(10L).seller(seller).build();
        SellerRating r1 = SellerRating.builder().id(1L).buyer(buyer).advertisement(ad).rating(5).build();
        SellerRating r2 = SellerRating.builder().id(2L).buyer(buyer).advertisement(ad).rating(4).build();

        when(sellerRatingRepository.findByAdvertisementSellerId(1L)).thenReturn(List.of(r1, r2));

        assertEquals(2L, sellerRatingService.getSellerRatingCount(1L));
    }

    // ==================== getSellerAverageRating ====================

    @Test
    void getSellerAverageRating_shouldReturnCorrectAverage() {
        User buyer = buyer();
        User seller = seller();
        Advertisement ad = Advertisement.builder().id(10L).seller(seller).build();
        SellerRating r1 = SellerRating.builder().id(1L).buyer(buyer).advertisement(ad).rating(5).build();
        SellerRating r2 = SellerRating.builder().id(2L).buyer(buyer).advertisement(ad).rating(3).build();

        when(sellerRatingRepository.findByAdvertisementSellerId(1L)).thenReturn(List.of(r1, r2));

        assertEquals(4.0, sellerRatingService.getSellerAverageRating(1L));
    }

    @Test
    void getSellerAverageRating_shouldReturnZero_whenSellerHasNoRatings() {
        when(sellerRatingRepository.findByAdvertisementSellerId(1L)).thenReturn(List.of());

        assertEquals(0.0, sellerRatingService.getSellerAverageRating(1L));
    }

    // ==================== getRatingSummariesForSellers ====================

    @Test
    void getRatingSummariesForSellers_shouldReturnEmptyMap_whenSellerIdsIsEmpty() {
        Map<Long, SellerRatingSummary> result = sellerRatingService.getRatingSummariesForSellers(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(sellerRatingRepository);
    }

    @Test
    void getRatingSummariesForSellers_shouldReturnEmptyMap_whenSellerIdsIsNull() {
        Map<Long, SellerRatingSummary> result = sellerRatingService.getRatingSummariesForSellers(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getRatingSummariesForSellers_shouldDefaultToEmptySummary_whenSellerHasNoAggregateRow() {
        when(sellerRatingRepository.findRatingAggregatesBySellerIds(List.of(1L))).thenReturn(List.of());

        Map<Long, SellerRatingSummary> result = sellerRatingService.getRatingSummariesForSellers(List.of(1L));

        assertEquals(1, result.size());
        assertEquals(0.0, result.get(1L).getAverageRating());
        assertEquals(0L, result.get(1L).getRatingCount());
    }

    @Test
    void getRatingSummariesForSellers_shouldDeduplicateSellerIds() {
        when(sellerRatingRepository.findRatingAggregatesBySellerIds(List.of(1L))).thenReturn(List.of());

        sellerRatingService.getRatingSummariesForSellers(List.of(1L, 1L, 1L));

        verify(sellerRatingRepository, times(1)).findRatingAggregatesBySellerIds(List.of(1L));
    }
}
