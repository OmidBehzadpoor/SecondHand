package com.example.secondhand.service;

import com.example.secondhand.dto.response.AdminDashboardResponse;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private SellerRatingRepository sellerRatingRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void getDashboard_shouldAggregateCountsFromAllRepositories() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(90L);
        when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(10L);

        when(advertisementRepository.count()).thenReturn(50L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.PENDING)).thenReturn(5L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.APPROVED)).thenReturn(30L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.REJECTED)).thenReturn(3L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.SOLD)).thenReturn(10L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.DELETED)).thenReturn(2L);

        when(categoryRepository.count()).thenReturn(15L);
        when(cityRepository.count()).thenReturn(8L);
        when(conversationRepository.count()).thenReturn(40L);
        when(messageRepository.count()).thenReturn(200L);
        when(favoriteRepository.count()).thenReturn(60L);
        when(sellerRatingRepository.count()).thenReturn(25L);

        AdminDashboardResponse response = adminDashboardService.getDashboard();

        assertEquals(100L, response.getTotalUsers());
        assertEquals(90L, response.getActiveUsers());
        assertEquals(10L, response.getBlockedUsers());
        assertEquals(50L, response.getTotalAdvertisements());
        assertEquals(5L, response.getPendingAdvertisements());
        assertEquals(30L, response.getApprovedAdvertisements());
        assertEquals(3L, response.getRejectedAdvertisements());
        assertEquals(10L, response.getSoldAdvertisements());
        assertEquals(2L, response.getDeletedAdvertisements());
        assertEquals(15L, response.getTotalCategories());
        assertEquals(8L, response.getTotalCities());
        assertEquals(40L, response.getTotalConversations());
        assertEquals(200L, response.getTotalMessages());
        assertEquals(60L, response.getTotalFavorites());
        assertEquals(25L, response.getTotalRatings());
    }

    @Test
    void getDashboard_shouldReturnAllZeros_whenDatabaseIsEmpty() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(0L);
        when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(0L);
        when(advertisementRepository.count()).thenReturn(0L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.PENDING)).thenReturn(0L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.APPROVED)).thenReturn(0L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.REJECTED)).thenReturn(0L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.SOLD)).thenReturn(0L);
        when(advertisementRepository.countByStatus(AdvertisementStatus.DELETED)).thenReturn(0L);
        when(categoryRepository.count()).thenReturn(0L);
        when(cityRepository.count()).thenReturn(0L);
        when(conversationRepository.count()).thenReturn(0L);
        when(messageRepository.count()).thenReturn(0L);
        when(favoriteRepository.count()).thenReturn(0L);
        when(sellerRatingRepository.count()).thenReturn(0L);

        AdminDashboardResponse response = adminDashboardService.getDashboard();

        assertEquals(0L, response.getTotalUsers());
        assertEquals(0L, response.getTotalAdvertisements());
    }
}
