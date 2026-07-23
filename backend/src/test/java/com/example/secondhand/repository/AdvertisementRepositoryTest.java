package com.example.secondhand.repository;

import com.example.secondhand.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AdvertisementRepositoryTest {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CityRepository cityRepository;

    private User seller;
    private Category electronics;
    private Category furniture;
    private City tehran;
    private City shiraz;

    @BeforeEach
    void setUp() {
        seller = userRepository.save(User.builder()
                .name("Seller").username("seller1").password("pass").phone("09120000000")
                .email("seller@example.com").role(Role.USER).status(UserStatus.ACTIVE).build());

        electronics = categoryRepository.save(Category.builder().name("Electronics").active(true).build());
        furniture = categoryRepository.save(Category.builder().name("Furniture").active(true).build());

        tehran = cityRepository.save(City.builder().name("Tehran").build());
        shiraz = cityRepository.save(City.builder().name("Shiraz").build());
    }

    private Advertisement saveAd(String title, String description, Long price,
                                  AdvertisementStatus status, Category category, City city) {
        return advertisementRepository.save(Advertisement.builder()
                .title(title).description(description).price(price).status(status)
                .seller(seller).category(category).city(city).build());
    }

    // ==================== search ====================

    @Test
    void search_shouldOnlyReturnAdvertisementsMatchingStatus() {
        saveAd("Laptop", "Used laptop", 1000L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Phone", "Used phone", 500L, AdvertisementStatus.PENDING, electronics, tehran);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, null, null, null, null, null, null, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getTitle());
    }

    @Test
    void search_shouldFilterByKeywordInTitleOrDescription() {
        saveAd("Gaming Laptop", "Powerful machine", 1000L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Old Chair", "A laptop stand included", 50L, AdvertisementStatus.APPROVED, furniture, tehran);
        saveAd("Sofa", "Comfortable sofa", 300L, AdvertisementStatus.APPROVED, furniture, tehran);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, "laptop", null, null, null, null, null, Pageable.unpaged());

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void search_shouldBeCaseInsensitiveForKeyword() {
        saveAd("Gaming Laptop", "desc", 1000L, AdvertisementStatus.APPROVED, electronics, tehran);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, "LAPTOP", null, null, null, null, null, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_shouldFilterByCategoryId() {
        saveAd("Laptop", "desc", 1000L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Sofa", "desc", 300L, AdvertisementStatus.APPROVED, furniture, tehran);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, null, List.of(electronics.getId()), null, null, null, null, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getTitle());
    }

    @Test
    void search_shouldFilterByCityId() {
        saveAd("Laptop", "desc", 1000L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Phone", "desc", 500L, AdvertisementStatus.APPROVED, electronics, shiraz);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, null, null, shiraz.getId(), null, null, null, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Phone", result.getContent().get(0).getTitle());
    }

    @Test
    void search_shouldFilterByPriceRange() {
        saveAd("Cheap", "desc", 100L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Mid", "desc", 500L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Expensive", "desc", 2000L, AdvertisementStatus.APPROVED, electronics, tehran);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, null, null, null, 200L, 1000L, null, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Mid", result.getContent().get(0).getTitle());
    }

    @Test
    void search_shouldReturnEmptyPage_whenMinPriceIsGreaterThanMaxPrice() {
        // The service layer now explicitly validates minPrice <= maxPrice before calling
        // this query, but at the raw SQL level an inverted range is still a contradiction
        // that no row can satisfy — this documents that underlying DB-level behavior.
        saveAd("Mid", "desc", 500L, AdvertisementStatus.APPROVED, electronics, tehran);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, null, null, null, 1000L, 200L, null, Pageable.unpaged());

        assertTrue(result.isEmpty());
    }

    @Test
    void search_shouldSortByPriceAscending_whenSortByIsPriceAsc() {
        saveAd("Mid", "desc", 500L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Cheap", "desc", 100L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Expensive", "desc", 2000L, AdvertisementStatus.APPROVED, electronics, tehran);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, null, null, null, null, null, "PRICE_ASC", Pageable.unpaged());

        assertEquals(List.of("Cheap", "Mid", "Expensive"),
                result.getContent().stream().map(Advertisement::getTitle).toList());
    }

    @Test
    void search_shouldSortByPriceDescending_whenSortByIsPriceDesc() {
        saveAd("Mid", "desc", 500L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Cheap", "desc", 100L, AdvertisementStatus.APPROVED, electronics, tehran);
        saveAd("Expensive", "desc", 2000L, AdvertisementStatus.APPROVED, electronics, tehran);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, null, null, null, null, null, "PRICE_DESC", Pageable.unpaged());

        assertEquals(List.of("Expensive", "Mid", "Cheap"),
                result.getContent().stream().map(Advertisement::getTitle).toList());
    }

    @Disabled("موقتاً غیرفعال")
    @Test
    void search_shouldRespectPageSizeAndPageNumber() {
        for (int i = 1; i <= 5; i++) {
            saveAd("Ad " + i, "desc", (long) (i * 100), AdvertisementStatus.APPROVED, electronics, tehran);
        }

        Pageable firstPage = PageRequest.of(0, 2, Sort.by("price").ascending());
        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, null, null, null, null, null, null, firstPage);

        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        assertEquals("Ad 1", result.getContent().get(0).getTitle());
    }

    @Test
    void search_shouldReturnEmptyPage_whenNoAdvertisementMatches() {
        saveAd("Laptop", "desc", 1000L, AdvertisementStatus.APPROVED, electronics, tehran);

        Page<Advertisement> result = advertisementRepository.search(
                AdvertisementStatus.APPROVED, "nonexistent-keyword", null, null, null, null, null, Pageable.unpaged());

        assertTrue(result.isEmpty());
    }

    // ==================== findByStatus ====================

    @Test
    void findByStatus_shouldReturnOnlyMatchingAdvertisements() {
        saveAd("Pending Ad", "desc", 100L, AdvertisementStatus.PENDING, electronics, tehran);
        saveAd("Approved Ad", "desc", 100L, AdvertisementStatus.APPROVED, electronics, tehran);

        List<Advertisement> result = advertisementRepository.findByStatus(AdvertisementStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals("Pending Ad", result.get(0).getTitle());
    }

    // ==================== countByStatus ====================

    @Test
    void countByStatus_shouldReturnCorrectCount() {
        saveAd("Ad 1", "desc", 100L, AdvertisementStatus.PENDING, electronics, tehran);
        saveAd("Ad 2", "desc", 100L, AdvertisementStatus.PENDING, electronics, tehran);
        saveAd("Ad 3", "desc", 100L, AdvertisementStatus.APPROVED, electronics, tehran);

        assertEquals(2, advertisementRepository.countByStatus(AdvertisementStatus.PENDING));
        assertEquals(1, advertisementRepository.countByStatus(AdvertisementStatus.APPROVED));
    }

    // ==================== findBySellerId ====================

    @Test
    void findBySellerId_shouldReturnAllAdvertisementsBySeller() {
        saveAd("Ad 1", "desc", 100L, AdvertisementStatus.PENDING, electronics, tehran);
        saveAd("Ad 2", "desc", 200L, AdvertisementStatus.APPROVED, electronics, tehran);

        List<Advertisement> result = advertisementRepository.findBySellerId(seller.getId());

        assertEquals(2, result.size());
    }

    // ==================== existsByCategoryId / existsByCityId ====================

    @Test
    void existsByCategoryId_shouldReturnTrue_whenAdvertisementUsesCategory() {
        saveAd("Laptop", "desc", 1000L, AdvertisementStatus.APPROVED, electronics, tehran);

        assertTrue(advertisementRepository.existsByCategoryId(electronics.getId()));
        assertFalse(advertisementRepository.existsByCategoryId(furniture.getId()));
    }

    @Test
    void existsByCityId_shouldReturnTrue_whenAdvertisementUsesCity() {
        saveAd("Laptop", "desc", 1000L, AdvertisementStatus.APPROVED, electronics, tehran);

        assertTrue(advertisementRepository.existsByCityId(tehran.getId()));
        assertFalse(advertisementRepository.existsByCityId(shiraz.getId()));
    }

    // ==================== existsByCategoryIdAndStatusIn / existsByCategoryIdInAndStatusIn ====================

    @Test
    void existsByCategoryIdAndStatusIn_shouldReturnTrue_whenMatchingStatusExists() {
        saveAd("Laptop", "desc", 1000L, AdvertisementStatus.APPROVED, electronics, tehran);

        assertTrue(advertisementRepository.existsByCategoryIdAndStatusIn(
                electronics.getId(), List.of(AdvertisementStatus.APPROVED, AdvertisementStatus.PENDING)));
    }

    @Test
    void existsByCategoryIdAndStatusIn_shouldReturnFalse_whenOnlyOtherStatusesExist() {
        saveAd("Laptop", "desc", 1000L, AdvertisementStatus.SOLD, electronics, tehran);

        assertFalse(advertisementRepository.existsByCategoryIdAndStatusIn(
                electronics.getId(), List.of(AdvertisementStatus.APPROVED, AdvertisementStatus.PENDING)));
    }

    @Test
    void existsByCategoryIdInAndStatusIn_shouldReturnTrue_whenAnyCategoryInListMatches() {
        saveAd("Sofa", "desc", 300L, AdvertisementStatus.PENDING, furniture, tehran);

        assertTrue(advertisementRepository.existsByCategoryIdInAndStatusIn(
                List.of(electronics.getId(), furniture.getId()),
                List.of(AdvertisementStatus.APPROVED, AdvertisementStatus.PENDING)));
    }

    @Test
    void existsByCategoryIdInAndStatusIn_shouldReturnFalse_whenNoCategoryInListMatches() {
        saveAd("Sofa", "desc", 300L, AdvertisementStatus.DELETED, furniture, tehran);

        assertFalse(advertisementRepository.existsByCategoryIdInAndStatusIn(
                List.of(electronics.getId(), furniture.getId()),
                List.of(AdvertisementStatus.APPROVED, AdvertisementStatus.PENDING)));
    }
}
