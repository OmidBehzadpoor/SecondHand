package com.example.secondhand.service;

import com.example.secondhand.dto.response.FavoriteResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.FavoriteAlreadyExistsException;
import com.example.secondhand.exception.FavoriteNotFoundException;
import com.example.secondhand.model.*;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    // ==================== addFavorite ====================

    @Test
    void addFavorite_shouldAddFavorite_whenAdvertisementIsApproved() {
        User user = User.builder().id(1L).username("buyer").build();
        Category category = Category.builder().id(1L).name("Electronics").build();
        City city = City.builder().id(1L).name("Tehran").build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .title("Laptop")
                .status(AdvertisementStatus.APPROVED)
                .category(category)
                .city(city)
                .images(new ArrayList<>())
                .build();

        Favorite savedFavorite = Favorite.builder()
                .id(1L)
                .advertisement(ad)
                .user(user)
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(favoriteRepository.existsByUserIdAndAdvertisementId(1L, 10L)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(savedFavorite);

        FavoriteResponse response = favoriteService.addFavorite(10L, user);

        assertNotNull(response);
        assertEquals(10L, response.getAdvertisementId());
        assertEquals("Laptop", response.getAdvertisementTitle());
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void addFavorite_shouldAddFavorite_whenAdvertisementIsSold() {
        User user = User.builder().id(1L).username("buyer").build();
        Category category = Category.builder().id(1L).name("Electronics").build();
        City city = City.builder().id(1L).name("Tehran").build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .title("Laptop")
                .status(AdvertisementStatus.SOLD)
                .category(category)
                .city(city)
                .images(new ArrayList<>())
                .build();

        Favorite savedFavorite = Favorite.builder()
                .id(1L)
                .advertisement(ad)
                .user(user)
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(favoriteRepository.existsByUserIdAndAdvertisementId(1L, 10L)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(savedFavorite);

        FavoriteResponse response = favoriteService.addFavorite(10L, user);

        assertNotNull(response);
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
    }

    @Test
    void addFavorite_shouldThrowAdvertisementNotFoundException_whenAdDoesNotExist() {
        User user = User.builder().id(1L).build();

        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> favoriteService.addFavorite(99L, user));

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_shouldThrowAdvertisementNotFoundException_whenAdIsPending() {
        User user = User.builder().id(1L).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.PENDING)
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> favoriteService.addFavorite(10L, user));

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_shouldThrowAdvertisementNotFoundException_whenAdIsDeleted() {
        User user = User.builder().id(1L).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.DELETED)
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> favoriteService.addFavorite(10L, user));

        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addFavorite_shouldThrowFavoriteAlreadyExistsException_whenAlreadyFavorited() {
        User user = User.builder().id(1L).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.APPROVED)
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(favoriteRepository.existsByUserIdAndAdvertisementId(1L, 10L)).thenReturn(true);

        assertThrows(FavoriteAlreadyExistsException.class,
                () -> favoriteService.addFavorite(10L, user));

        verify(favoriteRepository, never()).save(any());
    }

    // ==================== removeFavorite ====================

    @Test
    void removeFavorite_shouldRemoveFavorite_whenExists() {
        User user = User.builder().id(1L).build();

        when(advertisementRepository.existsById(10L)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndAdvertisementId(1L, 10L)).thenReturn(true);

        favoriteService.removeFavorite(10L, user);

        verify(favoriteRepository, times(1)).deleteByUserIdAndAdvertisementId(1L, 10L);
    }

    @Test
    void removeFavorite_shouldThrowAdvertisementNotFoundException_whenAdDoesNotExist() {
        User user = User.builder().id(1L).build();

        when(advertisementRepository.existsById(99L)).thenReturn(false);

        assertThrows(AdvertisementNotFoundException.class,
                () -> favoriteService.removeFavorite(99L, user));

        verify(favoriteRepository, never()).deleteByUserIdAndAdvertisementId(any(), any());
    }

    @Test
    void removeFavorite_shouldThrowFavoriteNotFoundException_whenNotInFavorites() {
        User user = User.builder().id(1L).build();

        when(advertisementRepository.existsById(10L)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndAdvertisementId(1L, 10L)).thenReturn(false);

        assertThrows(FavoriteNotFoundException.class,
                () -> favoriteService.removeFavorite(10L, user));

        verify(favoriteRepository, never()).deleteByUserIdAndAdvertisementId(any(), any());
    }

    // ==================== getMyFavorites ====================

    @Test
    void getMyFavorites_shouldReturnFavorites_whenUserHasSome() {
        User user = User.builder().id(1L).build();
        Category category = Category.builder().id(1L).name("Electronics").build();
        City city = City.builder().id(1L).name("Tehran").build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .title("Laptop")
                .status(AdvertisementStatus.APPROVED)
                .category(category)
                .city(city)
                .images(new ArrayList<>())
                .build();

        Favorite favorite = Favorite.builder()
                .id(1L)
                .advertisement(ad)
                .user(user)
                .build();

        when(favoriteRepository.findByUserId(1L)).thenReturn(List.of(favorite));

        List<FavoriteResponse> responses = favoriteService.getMyFavorites(user);

        assertEquals(1, responses.size());
        assertEquals("Laptop", responses.get(0).getAdvertisementTitle());
        assertEquals(10L, responses.get(0).getAdvertisementId());
    }

    @Test
    void getMyFavorites_shouldReturnEmptyList_whenUserHasNone() {
        User user = User.builder().id(1L).build();

        when(favoriteRepository.findByUserId(1L)).thenReturn(List.of());

        List<FavoriteResponse> responses = favoriteService.getMyFavorites(user);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}