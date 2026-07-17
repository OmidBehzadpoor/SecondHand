package com.example.secondhand.service;

import com.example.secondhand.dto.AdvertisementRequest;
import com.example.secondhand.dto.response.AdvertisementResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.CategoryNotFoundException;
import com.example.secondhand.exception.InvalidAdvertisementStateException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.*;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CategoryRepository;
import com.example.secondhand.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {

    @Mock
    private AdvertisementRepository advertisementRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private AdvertisementService advertisementService;

    @Test
    void markAsSold_shouldThrow_whenStatusIsPending() {

        // ARRANGE
        User seller = User.builder().id(1L).build();
        Advertisement ad = Advertisement.builder()
                .id(1L)
                .seller(seller)
                .status(AdvertisementStatus.PENDING)
                .images(new ArrayList<>())
                .build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        // ACT + ASSERT
        assertThrows(InvalidAdvertisementStateException.class,
                () -> advertisementService.markAsSold(1L, seller));
    }

    @Test
    void markAsSold_shouldModifyStatusToSold_whenAdvertisementIsApprovedAndUserIsSeller() {
        User seller = User.builder().id(1L).build();
        Advertisement ad = Advertisement.builder()
                .id(1L)
                .seller(seller)
                .status(AdvertisementStatus.APPROVED)
                .images(new ArrayList<>())
                .build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(advertisementRepository.save(any(Advertisement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        advertisementService.markAsSold(1L, seller);

        assertEquals(AdvertisementStatus.SOLD, ad.getStatus());
        verify(advertisementRepository, times(1)).save(ad);
    }

    @Test
    void markAsSold_shouldThrow_whenUserIsNotTheSeller() {

        User seller = User.builder().id(1L).build();
        User isNotSeller = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder()
                .id(1L)
                .seller(seller)
                .status(AdvertisementStatus.APPROVED)
                .images(new ArrayList<>())
                .build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class, () -> advertisementService.markAsSold(1L, isNotSeller));
    }

    @Test
    void markAsSold_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {

        User seller = User.builder().id(1L).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class, () -> advertisementService.markAsSold(1L, seller));
    }

    @Test
    void delete_shouldModifyStatusToDeleted_whenUserIsSeller(){
        User seller = User.builder().id(1L).build();

        Advertisement ad = Advertisement.builder().id(1L).seller(seller).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        advertisementService.delete(1L, seller);

        assertEquals(AdvertisementStatus.DELETED, ad.getStatus());
        verify(advertisementRepository, times(1)).save(ad);
    }

    @Test
    void delete_shouldThrowUnauthorizedActionException_whenUserIsNotSeller(){
        User seller = User.builder().id(1L).build();
        User isNotSeller = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder()
                .id(1L)
                .seller(seller)
                .images(new ArrayList<>())
                .build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class, () -> advertisementService.delete(1L, isNotSeller));

        verify(advertisementRepository, never()).save(any(Advertisement.class));
    }

    @Test
    void delete_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {

        User seller = User.builder().id(1L).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class, () -> advertisementService.delete(1L, seller));

        verify(advertisementRepository, never()).save(any(Advertisement.class));
    }

    @Test
    void delete_shouldThrowInvalidAdvertisementStateException_whenAdvertisementIsAlreadyDeleted() {

        User seller = User.builder().id(1L).build();

        Advertisement ad = Advertisement.builder()
                .id(1L)
                .seller(seller)
                .status(AdvertisementStatus.DELETED)
                .build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidAdvertisementStateException.class, () -> advertisementService.delete(1L, seller));

        verify(advertisementRepository, never()).save(any(Advertisement.class));
    }

    @Test
    void create_shouldSaveAdvertisementSuccessfully_whenRequestIsValid() {
        User currentUser = User.builder()
                .id(1L)
                .username("omid_b")
                .name("Omid Behzadpoor")
                .build();

        Category category = Category.builder()
                .id(10L)
                .name("Electronics")
                .build();

        City city = City.builder()
                .id(20L)
                .name("Tehran")
                .build();

        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("Laptop")
                .description("Core i7, 16GB RAM")
                .price(1500L)
                .categoryId(10L)
                .cityId(20L)
                .imageUrls(List.of("http://example.com/image.jpg"))
                .build();

        Advertisement savedAd = Advertisement.builder()
                .id(100L)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .city(city)
                .seller(currentUser)
                .status(AdvertisementStatus.PENDING)
                .images(new ArrayList<>())
                .build();

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(cityRepository.findById(20L)).thenReturn(Optional.of(city));
        when(advertisementRepository.save(any(Advertisement.class))).thenReturn(savedAd);

        AdvertisementResponse response = advertisementService.create(request, currentUser);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Laptop", response.getTitle());
        assertEquals("Tehran", response.getCityName());
        assertEquals("Electronics", response.getCategoryName());
        assertEquals(AdvertisementStatus.PENDING, response.getStatus());
        assertEquals(1L, response.getOwnerId());
        assertEquals("omid_b", response.getOwnerUsername());
        assertFalse(response.getImageUrls().isEmpty());

        verify(categoryRepository, times(1)).findById(10L);
        verify(cityRepository, times(1)).findById(20L);
        verify(advertisementRepository, times(1)).save(any(Advertisement.class));
    }

    @Test
    void create_shouldThrowCategoryNotFoundException_whenCategoryDoesNotExist() {
        User currentUser = User.builder().id(1L).build();

        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("Laptop")
                .categoryId(999L)
                .cityId(20L)
                .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> advertisementService.create(request, currentUser));

        verify(advertisementRepository, never()).save(any(Advertisement.class));
    }

}