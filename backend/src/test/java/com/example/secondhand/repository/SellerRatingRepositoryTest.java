package com.example.secondhand.repository;

import com.example.secondhand.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SellerRatingRepositoryTest {

    @Autowired
    private SellerRatingRepository sellerRatingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    private User seller;
    private User buyer;
    private Advertisement ad1;
    private Advertisement ad2;

    @BeforeEach
    void setUp() {
        seller = userRepository.save(User.builder()
                .name("Seller").username("seller1").password("pass").phone("09120000000")
                .email("seller@example.com").role(Role.USER).status(UserStatus.ACTIVE).build());

        buyer = userRepository.save(User.builder()
                .name("Buyer").username("buyer1").password("pass").phone("09121111111")
                .email("buyer@example.com").role(Role.USER).status(UserStatus.ACTIVE).build());

        Category category = categoryRepository.save(Category.builder().name("Electronics").build());
        City city = cityRepository.save(City.builder().name("Tehran").build());

        ad1 = advertisementRepository.save(Advertisement.builder()
                .title("Laptop").description("desc").price(1000L)
                .status(AdvertisementStatus.SOLD).seller(seller).category(category).city(city).build());

        ad2 = advertisementRepository.save(Advertisement.builder()
                .title("Phone").description("desc").price(500L)
                .status(AdvertisementStatus.SOLD).seller(seller).category(category).city(city).build());
    }

    @Test
    void existsByBuyerIdAndAdvertisementId_shouldReturnTrue_whenRatingExists() {
        sellerRatingRepository.save(SellerRating.builder().buyer(buyer).advertisement(ad1).rating(5).build());

        assertTrue(sellerRatingRepository.existsByBuyerIdAndAdvertisementId(buyer.getId(), ad1.getId()));
        assertFalse(sellerRatingRepository.existsByBuyerIdAndAdvertisementId(buyer.getId(), ad2.getId()));
    }

    @Test
    void findByAdvertisementSellerId_shouldReturnAllRatingsForSeller() {
        sellerRatingRepository.save(SellerRating.builder().buyer(buyer).advertisement(ad1).rating(5).build());
        sellerRatingRepository.save(SellerRating.builder().buyer(buyer).advertisement(ad2).rating(4).build());

        List<SellerRating> result = sellerRatingRepository.findByAdvertisementSellerId(seller.getId());

        assertEquals(2, result.size());
    }

    @Test
    void findByAdvertisementSellerId_shouldReturnEmptyList_whenSellerHasNoRatings() {
        assertTrue(sellerRatingRepository.findByAdvertisementSellerId(seller.getId()).isEmpty());
    }

    @Test
    void save_shouldThrowDataIntegrityViolationException_whenSameBuyerRatesSameAdvertisementTwice() {
        sellerRatingRepository.saveAndFlush(SellerRating.builder().buyer(buyer).advertisement(ad1).rating(5).build());

        SellerRating duplicate = SellerRating.builder().buyer(buyer).advertisement(ad1).rating(3).build();

        assertThrows(DataIntegrityViolationException.class,
                () -> sellerRatingRepository.saveAndFlush(duplicate));
    }

    // ==================== findRatingAggregatesBySellerIds ====================

    @Test
    void findRatingAggregatesBySellerIds_shouldReturnCorrectAverageAndCount() {
        sellerRatingRepository.save(SellerRating.builder().buyer(buyer).advertisement(ad1).rating(5).build());
        sellerRatingRepository.save(SellerRating.builder().buyer(buyer).advertisement(ad2).rating(3).build());

        List<SellerRatingRepository.SellerRatingAggregate> result =
                sellerRatingRepository.findRatingAggregatesBySellerIds(List.of(seller.getId()));

        assertEquals(1, result.size());
        assertEquals(seller.getId(), result.get(0).getSellerId());
        assertEquals(4.0, result.get(0).getAverageRating());
        assertEquals(2L, result.get(0).getRatingCount());
    }

    @Test
    void findRatingAggregatesBySellerIds_shouldReturnEmptyList_whenSellerHasNoRatings() {
        List<SellerRatingRepository.SellerRatingAggregate> result =
                sellerRatingRepository.findRatingAggregatesBySellerIds(List.of(seller.getId()));

        assertTrue(result.isEmpty());
    }

    @Test
    void findRatingAggregatesBySellerIds_shouldOnlyIncludeRequestedSellers() {
        User otherSeller = userRepository.save(User.builder()
                .name("Other Seller").username("otherseller").password("pass").phone("09124444444")
                .email("other@example.com").role(Role.USER).status(UserStatus.ACTIVE).build());
        Advertisement otherAd = advertisementRepository.save(Advertisement.builder()
                .title("Tablet").description("desc").price(700L).status(AdvertisementStatus.SOLD)
                .seller(otherSeller).category(ad1.getCategory()).city(ad1.getCity()).build());

        sellerRatingRepository.save(SellerRating.builder().buyer(buyer).advertisement(ad1).rating(5).build());
        sellerRatingRepository.save(SellerRating.builder().buyer(buyer).advertisement(otherAd).rating(2).build());

        List<SellerRatingRepository.SellerRatingAggregate> result =
                sellerRatingRepository.findRatingAggregatesBySellerIds(List.of(seller.getId()));

        assertEquals(1, result.size());
        assertEquals(seller.getId(), result.get(0).getSellerId());
    }
}
