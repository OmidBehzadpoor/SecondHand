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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

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
    private Advertisement ad;

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

        ad = advertisementRepository.save(Advertisement.builder()
                .title("Laptop").description("desc").price(1000L)
                .status(AdvertisementStatus.APPROVED).seller(seller).category(category).city(city).build());
    }

    @Test
    void findByAdvertisementIdAndBuyerId_shouldReturnConversation_whenItExists() {
        conversationRepository.save(Conversation.builder().advertisement(ad).buyer(buyer).build());

        Optional<Conversation> result =
                conversationRepository.findByAdvertisementIdAndBuyerId(ad.getId(), buyer.getId());

        assertTrue(result.isPresent());
    }

    @Test
    void findByAdvertisementIdAndBuyerId_shouldReturnEmpty_whenNoneExists() {
        assertTrue(conversationRepository.findByAdvertisementIdAndBuyerId(ad.getId(), buyer.getId()).isEmpty());
    }

    @Test
    void findByBuyerIdOrSellerId_shouldReturnConversation_whenUserIsBuyer() {
        conversationRepository.save(Conversation.builder().advertisement(ad).buyer(buyer).build());

        List<Conversation> result = conversationRepository
                .findByBuyerIdOrAdvertisementSellerIdOrderByUpdatedAtDesc(buyer.getId(), buyer.getId());

        assertEquals(1, result.size());
    }

    @Test
    void findByBuyerIdOrSellerId_shouldReturnConversation_whenUserIsSeller() {
        conversationRepository.save(Conversation.builder().advertisement(ad).buyer(buyer).build());

        List<Conversation> result = conversationRepository
                .findByBuyerIdOrAdvertisementSellerIdOrderByUpdatedAtDesc(seller.getId(), seller.getId());

        assertEquals(1, result.size());
    }

    @Test
    void findByBuyerIdOrSellerId_shouldReturnEmptyList_whenUserIsNeitherBuyerNorSeller() {
        conversationRepository.save(Conversation.builder().advertisement(ad).buyer(buyer).build());

        User stranger = userRepository.save(User.builder()
                .name("Stranger").username("stranger1").password("pass").phone("09122222222")
                .email("stranger@example.com").role(Role.USER).status(UserStatus.ACTIVE).build());

        List<Conversation> result = conversationRepository
                .findByBuyerIdOrAdvertisementSellerIdOrderByUpdatedAtDesc(stranger.getId(), stranger.getId());

        assertTrue(result.isEmpty());
    }

    @Disabled("موقتاً غیرفعال")
    @Test
    void save_shouldThrowDataIntegrityViolationException_whenSameBuyerStartsSecondConversationOnSameAd() {
        conversationRepository.saveAndFlush(Conversation.builder().advertisement(ad).buyer(buyer).build());

        Conversation duplicate = Conversation.builder().advertisement(ad).buyer(buyer).build();

        assertThrows(DataIntegrityViolationException.class,
                () -> conversationRepository.saveAndFlush(duplicate));
    }
}
