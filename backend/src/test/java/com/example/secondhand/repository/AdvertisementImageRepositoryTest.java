package com.example.secondhand.repository;

import com.example.secondhand.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AdvertisementImageRepositoryTest {

    @Autowired
    private AdvertisementImageRepository advertisementImageRepository;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CityRepository cityRepository;

    private Advertisement ad;

    @BeforeEach
    void setUp() {
        User seller = userRepository.save(User.builder()
                .name("Seller").username("seller1").password("pass").phone("09120000000")
                .email("seller@example.com").role(Role.USER).status(UserStatus.ACTIVE).build());

        Category category = categoryRepository.save(Category.builder().name("Electronics").build());
        City city = cityRepository.save(City.builder().name("Tehran").build());

        ad = advertisementRepository.save(Advertisement.builder()
                .title("Laptop").description("desc").price(1000L)
                .status(AdvertisementStatus.APPROVED).seller(seller).category(category).city(city).build());
    }

    @Test
    void save_shouldPersistImageLinkedToAdvertisement() {
        AdvertisementImage image = advertisementImageRepository.save(
                AdvertisementImage.builder().imageUrl("/uploads/advertisements/1/photo.jpg").advertisement(ad).build());

        Optional<AdvertisementImage> reloaded = advertisementImageRepository.findById(image.getId());

        assertTrue(reloaded.isPresent());
        assertEquals("/uploads/advertisements/1/photo.jpg", reloaded.get().getImageUrl());
        assertEquals(ad.getId(), reloaded.get().getAdvertisement().getId());
    }

    @Test
    void delete_shouldRemoveImage() {
        AdvertisementImage image = advertisementImageRepository.save(
                AdvertisementImage.builder().imageUrl("/uploads/advertisements/1/photo.jpg").advertisement(ad).build());

        advertisementImageRepository.delete(image);

        assertTrue(advertisementImageRepository.findById(image.getId()).isEmpty());
    }

    @Test
    void findById_shouldReturnEmpty_whenImageDoesNotExist() {
        assertTrue(advertisementImageRepository.findById(999L).isEmpty());
    }
}
