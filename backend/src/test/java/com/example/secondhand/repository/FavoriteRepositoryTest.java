package com.example.secondhand.repository;

import com.example.secondhand.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
class FavoriteRepositoryTest {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    private User user;
    private Advertisement ad1;
    private Advertisement ad2;

    @BeforeEach
    void setUp() {
        User seller = userRepository.save(User.builder()
                .name("Seller").username("seller1").password("pass").phone("09120000000")
                .email("seller@example.com").role(Role.USER).status(UserStatus.ACTIVE).build());

        user = userRepository.save(User.builder()
                .name("Buyer").username("buyer1").password("pass").phone("09121111111")
                .email("buyer@example.com").role(Role.USER).status(UserStatus.ACTIVE).build());

        Category category = categoryRepository.save(Category.builder().name("Electronics").build());
        City city = cityRepository.save(City.builder().name("Tehran").build());

        ad1 = advertisementRepository.save(Advertisement.builder()
                .title("Laptop").description("desc").price(1000L)
                .status(AdvertisementStatus.APPROVED).seller(seller).category(category).city(city).build());

        ad2 = advertisementRepository.save(Advertisement.builder()
                .title("Phone").description("desc").price(500L)
                .status(AdvertisementStatus.APPROVED).seller(seller).category(category).city(city).build());
    }

    @Test
    void findByUserId_shouldReturnAllFavoritesForUser() {
        favoriteRepository.save(Favorite.builder().user(user).advertisement(ad1).build());
        favoriteRepository.save(Favorite.builder().user(user).advertisement(ad2).build());

        List<Favorite> result = favoriteRepository.findByUserId(user.getId());

        assertEquals(2, result.size());
    }

    @Test
    void findByUserId_shouldReturnEmptyList_whenUserHasNoFavorites() {
        assertTrue(favoriteRepository.findByUserId(user.getId()).isEmpty());
    }

    @Test
    void existsByUserIdAndAdvertisementId_shouldReturnTrue_whenFavoriteExists() {
        favoriteRepository.save(Favorite.builder().user(user).advertisement(ad1).build());

        assertTrue(favoriteRepository.existsByUserIdAndAdvertisementId(user.getId(), ad1.getId()));
        assertFalse(favoriteRepository.existsByUserIdAndAdvertisementId(user.getId(), ad2.getId()));
    }

    @Test
    void deleteByUserIdAndAdvertisementId_shouldRemoveOnlyMatchingFavorite() {
        favoriteRepository.save(Favorite.builder().user(user).advertisement(ad1).build());
        favoriteRepository.save(Favorite.builder().user(user).advertisement(ad2).build());

        favoriteRepository.deleteByUserIdAndAdvertisementId(user.getId(), ad1.getId());

        List<Favorite> remaining = favoriteRepository.findByUserId(user.getId());
        assertEquals(1, remaining.size());
        assertEquals(ad2.getId(), remaining.get(0).getAdvertisement().getId());
    }

    @Disabled("موقتاً غیرفعال")
    @Test
    void save_shouldThrowDataIntegrityViolationException_whenSameUserFavoritesSameAdvertisementTwice() {
        favoriteRepository.saveAndFlush(Favorite.builder().user(user).advertisement(ad1).build());

        Favorite duplicate = Favorite.builder().user(user).advertisement(ad1).build();

        assertThrows(DataIntegrityViolationException.class,
                () -> favoriteRepository.saveAndFlush(duplicate));
    }
}
