package com.example.secondhand.repository;

import com.example.secondhand.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

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
    private Conversation conversation;

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

        Advertisement ad = advertisementRepository.save(Advertisement.builder()
                .title("Laptop").description("desc").price(1000L)
                .status(AdvertisementStatus.APPROVED).seller(seller).category(category).city(city).build());

        conversation = conversationRepository.save(Conversation.builder().advertisement(ad).buyer(buyer).build());
    }

    private Message saveMessage(User sender, String content, boolean isRead) {
        return messageRepository.save(Message.builder()
                .conversation(conversation).sender(sender).content(content).isRead(isRead).build());
    }

    @Test
    void findByConversationIdOrderByCreatedAtAsc_shouldReturnMessagesInOrder() throws InterruptedException {
        saveMessage(buyer, "first", false);
        Thread.sleep(5);
        saveMessage(seller, "second", false);

        List<Message> result = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());

        assertEquals(2, result.size());
        assertEquals("first", result.get(0).getContent());
        assertEquals("second", result.get(1).getContent());
    }

    @Test
    void findFirstByConversationIdOrderByCreatedAtDesc_shouldReturnLatestMessage() throws InterruptedException {
        saveMessage(buyer, "first", false);
        Thread.sleep(5);
        saveMessage(seller, "latest", false);

        Optional<Message> result =
                messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());

        assertTrue(result.isPresent());
        assertEquals("latest", result.get().getContent());
    }

    @Test
    void findFirstByConversationIdOrderByCreatedAtDesc_shouldReturnEmpty_whenNoMessagesExist() {
        assertTrue(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId()).isEmpty());
    }

    @Test
    void countUnread_shouldOnlyCountUnreadMessagesFromOtherSender() {
        saveMessage(buyer, "unread from buyer", false);
        saveMessage(buyer, "already read from buyer", true);
        saveMessage(seller, "own unread message", false);

        long count = messageRepository.countByConversationIdAndIsReadFalseAndSenderIdNot(
                conversation.getId(), seller.getId());

        assertEquals(1, count);
    }

    @Test
    void markMessagesAsRead_shouldMarkOnlyMessagesFromOtherSenderAsRead() {
        Message fromBuyer = saveMessage(buyer, "hi seller", false);
        Message fromSeller = saveMessage(seller, "hi buyer", false);

        messageRepository.markMessagesAsRead(conversation.getId(), seller.getId());

        Message reloadedFromBuyer = messageRepository.findById(fromBuyer.getId()).orElseThrow();
        Message reloadedFromSeller = messageRepository.findById(fromSeller.getId()).orElseThrow();

        assertTrue(reloadedFromBuyer.isRead());
        assertFalse(reloadedFromSeller.isRead());
    }

    @Test
    void markMessagesAsRead_shouldNotAffectAlreadyReadMessages() {
        Message alreadyRead = saveMessage(buyer, "old message", true);

        messageRepository.markMessagesAsRead(conversation.getId(), seller.getId());

        assertTrue(messageRepository.findById(alreadyRead.getId()).orElseThrow().isRead());
    }
}
