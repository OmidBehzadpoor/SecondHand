package com.example.secondhand.service;

import com.example.secondhand.dto.AdvertisementRequest;
import com.example.secondhand.dto.response.AdvertisementResponse;
import com.example.secondhand.exception.*;
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
import java.util.List;
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

    @Test
    void create_shouldThrowCityNotFoundException_whenCityDoesNotExist() {
        User currentUser = User.builder().id(1L).build();
        Category category = Category.builder().id(10L).name("Electronics").build();

        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("Laptop")
                .categoryId(10L)
                .cityId(888L)
                .build();

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(cityRepository.findById(888L)).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class,
                () -> advertisementService.create(request, currentUser));

        verify(advertisementRepository, never()).save(any(Advertisement.class));
    }
    @Test
    void create_shouldSaveAdvertisementSuccessfully_whenNoImagesAreProvided() {
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
                .imageUrls(null)
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
        assertTrue(response.getImageUrls() == null || response.getImageUrls().isEmpty());

        verify(advertisementRepository, times(1)).save(any(Advertisement.class));
    }

    @Test
    void create_shouldThrowUnauthorizedActionException_whenUserIsAnonymous() {
        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("Laptop")
                .description("Core i7, 16GB RAM")
                .price(1500L)
                .categoryId(10L)
                .cityId(20L)
                .build();

        assertThrows(UnauthorizedActionException.class,
                () -> advertisementService.create(request, null));

        verify(advertisementRepository, never()).save(any(Advertisement.class));
    }

    @Test
    void getById_shouldReturnAdvertisementResponse_whenAdvertisementExistsAndIsAPPROVED() {
        // Arrange
        Long adId = 100L;
        User seller = User.builder().id(1L).username("seller_user").build();
        User viewer = User.builder().id(2L).username("viewer_user").build();

        Advertisement ad = Advertisement.builder()
                .id(adId)
                .title("iPhone 13")
                .description("In good condition")
                .price(800L)
                .status(AdvertisementStatus.APPROVED)
                .seller(seller)
                .build();

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));

        // Act
        AdvertisementResponse response = advertisementService.getById(adId, viewer);

        // Assert
        assertNotNull(response);
        assertEquals(adId, response.getId());
        assertEquals("iPhone 13", response.getTitle());
        verify(advertisementRepository, times(1)).findById(adId);
    }

    @Test
    void getById_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {

        Long nonExistentAdId = 999L;
        User currentUser = User.builder().id(1L).build();

        when(advertisementRepository.findById(nonExistentAdId)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.getById(nonExistentAdId, currentUser));

        verify(advertisementRepository, never()).save(any(Advertisement.class));
    }

    @Test
    void getById_shouldThrowAdvertisementNotFoundException_whenAdvertisementIsInactiveAndUserIsNotOwner() {
        // Arrange
        Long adId = 100L;
        User owner = User.builder().id(1L).username("owner_user").build();
        User stranger = User.builder().id(2L).username("stranger_user").build();

        Advertisement pendingAd = Advertisement.builder()
                .id(adId)
                .title("Draft Item")
                .status(AdvertisementStatus.PENDING)
                .seller(owner)
                .build();

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(pendingAd));

        // Act & Assert
        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.getById(adId, stranger));
    }

    @Test
    void getById_shouldReturnAdvertisementResponse_whenAdvertisementIsPendingButUserIsOwner() {
        Long adId = 100L;
        User owner = User.builder().id(1L).username("owner_user").build();

        Advertisement pendingAd = Advertisement.builder()
                .id(adId)
                .title("Draft Item")
                .status(AdvertisementStatus.PENDING)
                .seller(owner)
                .build();

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(pendingAd));

        AdvertisementResponse response = advertisementService.getById(adId, owner);

        assertNotNull(response);
        assertEquals(adId, response.getId());
        assertEquals("Draft Item", response.getTitle());
    }

    // ==================== UPDATE ====================

    @Test
    void update_shouldUpdateAdvertisement_whenUserIsOwnerAndDataIsValid() {
        User seller = User.builder().id(1L).username("omid_b").name("Omid").build();
        Category oldCategory = Category.builder().id(1L).name("Old").build();
        Category newCategory = Category.builder().id(2L).name("Electronics").build();
        City oldCity = City.builder().id(1L).name("Tehran").build();
        City newCity = City.builder().id(2L).name("Isfahan").build();

        Advertisement ad = Advertisement.builder()
                .id(1L)
                .title("Old Title")
                .description("Old Desc")
                .price(100L)
                .category(oldCategory)
                .city(oldCity)
                .seller(seller)
                .status(AdvertisementStatus.APPROVED)
                .images(new ArrayList<>())
                .build();

        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("New Title")
                .description("New Desc")
                .price(200L)
                .categoryId(2L)
                .cityId(2L)
                .imageUrls(List.of())
                .build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(cityRepository.findById(2L)).thenReturn(Optional.of(newCity));
        when(advertisementRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AdvertisementResponse response = advertisementService.update(1L, request, seller);

        assertEquals("New Title", response.getTitle());
        assertEquals("New Desc", response.getDescription());
        assertEquals(200L, response.getPrice());
        assertEquals(AdvertisementStatus.PENDING, response.getStatus()); // باید برگرده به pending بعد از ویرایش
        verify(advertisementRepository, times(1)).save(any());
    }

    @Test
    void update_shouldThrowUnauthorizedActionException_whenUserIsNotOwner() {
        User seller = User.builder().id(1L).build();
        User stranger = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder()
                .id(1L)
                .seller(seller)
                .images(new ArrayList<>())
                .build();

        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("New Title")
                .categoryId(1L)
                .cityId(1L)
                .build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> advertisementService.update(1L, request, stranger));

        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {
        User seller = User.builder().id(1L).build();
        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("New Title")
                .categoryId(1L)
                .cityId(1L)
                .build();

        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.update(99L, request, seller));

        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowCategoryNotFoundException_whenCategoryDoesNotExist() {
        User seller = User.builder().id(1L).build();

        Advertisement ad = Advertisement.builder()
                .id(1L)
                .seller(seller)
                .images(new ArrayList<>())
                .build();

        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("New Title")
                .categoryId(999L)
                .cityId(1L)
                .build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> advertisementService.update(1L, request, seller));

        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowCityNotFoundException_whenCityDoesNotExist() {
        User seller = User.builder().id(1L).build();
        Category category = Category.builder().id(1L).name("Electronics").build();

        Advertisement ad = Advertisement.builder()
                .id(1L)
                .seller(seller)
                .images(new ArrayList<>())
                .build();

        AdvertisementRequest request = AdvertisementRequest.builder()
                .title("New Title")
                .categoryId(1L)
                .cityId(888L)
                .build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cityRepository.findById(888L)).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class,
                () -> advertisementService.update(1L, request, seller));

        verify(advertisementRepository, never()).save(any());
    }



    @Test
    void getAll_shouldReturnOnlyApprovedAdvertisements() {
        when(advertisementRepository.search(
                eq(AdvertisementStatus.APPROVED),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        List<AdvertisementResponse> result = advertisementService.getAll(null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(advertisementRepository, times(1))
                .search(eq(AdvertisementStatus.APPROVED), isNull(), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void getAll_shouldPassKeywordAndFiltersToRepository() {
        Category category = Category.builder().id(2L).name("Electronics").build();
        City city = City.builder().id(1L).name("Tehran").build();
        User seller = User.builder().id(1L).username("seller").build();

        Advertisement ad = Advertisement.builder()
                .id(1L)
                .title("Laptop")
                .description("Core i7")
                .price(1500L)
                .status(AdvertisementStatus.APPROVED)
                .category(category)
                .city(city)
                .seller(seller)
                .images(new ArrayList<>())
                .build();

        when(advertisementRepository.search(
                eq(AdvertisementStatus.APPROVED),
                eq("Laptop"), eq(2L), eq(1L), eq(1000L), eq(2000L), isNull()))
                .thenReturn(List.of(ad));

        List<AdvertisementResponse> result =
                advertisementService.getAll("Laptop", 2L, 1L, 1000L, 2000L, null);

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getTitle());
    }

// ==================== GET MY ADVERTISEMENTS ====================

    @Test
    void getMyAdvertisements_shouldReturnUserAdvertisements() {
        User seller = User.builder().id(1L).username("seller").build();
        Category category = Category.builder().id(1L).name("Electronics").build();
        City city = City.builder().id(1L).name("Tehran").build();

        Advertisement ad = Advertisement.builder()
                .id(1L)
                .title("My Laptop")
                .price(1000L)
                .status(AdvertisementStatus.APPROVED)
                .seller(seller)
                .category(category)
                .city(city)
                .images(new ArrayList<>())
                .build();

        when(advertisementRepository.findBySellerId(1L)).thenReturn(List.of(ad));

        List<AdvertisementResponse> result = advertisementService.getMyAdvertisements(seller);

        assertEquals(1, result.size());
        assertEquals("My Laptop", result.get(0).getTitle());
        assertEquals(1L, result.get(0).getOwnerId());
    }

    @Test
    void getMyAdvertisements_shouldReturnEmptyList_whenUserHasNoAdvertisements() {
        User seller = User.builder().id(1L).build();

        when(advertisementRepository.findBySellerId(1L)).thenReturn(List.of());

        List<AdvertisementResponse> result = advertisementService.getMyAdvertisements(seller);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

// ==================== GET BY ID ====================

    @Test
    void getById_shouldReturnAdvertisement_whenAdminViewsPendingAdvertisement() {
        Long adId = 1L;
        User owner = User.builder().id(1L).username("owner").build();
        User admin = User.builder().id(99L).username("admin").role(Role.ADMIN).build();

        Advertisement ad = Advertisement.builder()
                .id(adId)
                .title("Pending Ad")
                .status(AdvertisementStatus.PENDING)
                .seller(owner)
                .images(new ArrayList<>())
                .build();

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));

        AdvertisementResponse response = advertisementService.getById(adId, admin);

        assertNotNull(response);
        assertEquals("Pending Ad", response.getTitle());
    }

    @Test
    void getById_shouldThrowAdvertisementNotFoundException_whenAdvertisementIsDeleted() {
        Long adId = 1L;
        User user = User.builder().id(2L).build();
        User owner = User.builder().id(1L).build();

        Advertisement ad = Advertisement.builder()
                .id(adId)
                .title("Deleted Ad")
                .status(AdvertisementStatus.DELETED)
                .seller(owner)
                .build();

        when(advertisementRepository.findById(adId)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.getById(adId, user));
    }

}