package com.example.secondhand.service;

import com.example.secondhand.dto.AdvertisementRequest;
import com.example.secondhand.dto.SellerRatingSummary;
import com.example.secondhand.dto.response.AdminAdvertisementResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {

    @Mock
    private AdvertisementRepository advertisementRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private SellerRatingService sellerRatingService;

    @InjectMocks
    private AdvertisementService advertisementService;

    private User seller() {
        return User.builder().id(1L).username("omid_b").role(Role.USER).status(UserStatus.ACTIVE).build();
    }

    private Category category() {
        return Category.builder().id(1L).name("Electronics").active(true).build();
    }

    private City city() {
        return City.builder().id(1L).name("Tehran").build();
    }

    private AdvertisementRequest validRequest() {
        AdvertisementRequest request = new AdvertisementRequest();
        request.setTitle("Laptop");
        request.setDescription("Used laptop in good condition");
        request.setPrice(1000L);
        request.setCategoryId(1L);
        request.setCityId(1L);
        request.setImageUrls(List.of());
        return request;
    }

    private void stubEmptyRatingSummary() {
        when(sellerRatingService.getRatingSummariesForSellers(anyList())).thenReturn(Map.of());
    }

    // ==================== create ====================

    @Test
    void create_shouldCreateAdvertisement_whenDataIsValid() {
        User seller = seller();
        Category category = category();
        City city = city();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(advertisementRepository.save(any(Advertisement.class))).thenAnswer(i -> {
            Advertisement ad = i.getArgument(0);
            ad.setId(1L);
            return ad;
        });
        stubEmptyRatingSummary();

        AdvertisementResponse response = advertisementService.create(validRequest(), seller);

        assertEquals("Laptop", response.getTitle());
        assertEquals(AdvertisementStatus.PENDING, response.getStatus());
        assertEquals(1L, response.getOwnerId());
        verify(advertisementRepository, times(1)).save(any());
    }

    @Test
    void create_shouldThrowCategoryNotFoundException_whenCategoryDoesNotExist() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> advertisementService.create(validRequest(), seller()));

        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowCityNotFoundException_whenCityDoesNotExist() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category()));
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class,
                () -> advertisementService.create(validRequest(), seller()));

        verify(advertisementRepository, never()).save(any());
    }

    // ==================== getById ====================

    @Test
    void getById_shouldReturnAdvertisement_whenApprovedAndViewerIsStranger() {
        User seller = seller();
        User stranger = User.builder().id(2L).role(Role.USER).build();

        Advertisement ad = Advertisement.builder()
                .id(1L).title("Laptop").seller(seller).category(category()).city(city())
                .status(AdvertisementStatus.APPROVED).images(new ArrayList<>()).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        stubEmptyRatingSummary();

        AdvertisementResponse response = advertisementService.getById(1L, stranger);

        assertEquals("Laptop", response.getTitle());
    }

    @Test
    void getById_shouldThrowAdvertisementNotFoundException_whenPendingAndViewerIsStranger() {
        User seller = seller();
        User stranger = User.builder().id(2L).role(Role.USER).build();

        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller).category(category()).city(city())
                .status(AdvertisementStatus.PENDING).images(new ArrayList<>()).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.getById(1L, stranger));
    }

    @Test
    void getById_shouldReturnAdvertisement_whenPendingAndViewerIsOwner() {
        User seller = seller();

        Advertisement ad = Advertisement.builder()
                .id(1L).title("Laptop").seller(seller).category(category()).city(city())
                .status(AdvertisementStatus.PENDING).images(new ArrayList<>()).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        stubEmptyRatingSummary();

        AdvertisementResponse response = advertisementService.getById(1L, seller);

        assertEquals("Laptop", response.getTitle());
    }

    @Test
    void getById_shouldReturnAdvertisement_whenPendingAndViewerIsAdmin() {
        User seller = seller();
        User admin = User.builder().id(9L).role(Role.ADMIN).build();

        Advertisement ad = Advertisement.builder()
                .id(1L).title("Laptop").seller(seller).category(category()).city(city())
                .status(AdvertisementStatus.PENDING).images(new ArrayList<>()).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        stubEmptyRatingSummary();

        AdvertisementResponse response = advertisementService.getById(1L, admin);

        assertEquals("Laptop", response.getTitle());
    }

    @Test
    void getById_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {
        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.getById(99L, seller()));
    }

    @Test
    void getById_shouldThrowAdvertisementNotFoundException_whenViewerIsAnonymousAndAdNotPubliclyVisible() {
        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller()).category(category()).city(city())
                .status(AdvertisementStatus.PENDING).images(new ArrayList<>()).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.getById(1L, null));
    }

    // ==================== getAll ====================

    @Test
    void getAll_shouldThrowInvalidAdvertisementStateException_whenMinPriceIsGreaterThanMaxPrice() {
        assertThrows(InvalidAdvertisementStateException.class,
                () -> advertisementService.getAll(null, null, null, 1000L, 200L, null, Pageable.unpaged()));

        verify(advertisementRepository, never())
                .search(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void getAll_shouldReturnMappedPage_whenFiltersMatch() {
        Advertisement ad = Advertisement.builder()
                .id(1L).title("Laptop").seller(seller()).category(category()).city(city())
                .status(AdvertisementStatus.APPROVED).images(new ArrayList<>()).build();

        Page<Advertisement> page = new PageImpl<>(List.of(ad));

        when(advertisementRepository.search(eq(AdvertisementStatus.APPROVED), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(page);
        stubEmptyRatingSummary();

        Page<AdvertisementResponse> result =
                advertisementService.getAll(null, null, null, null, null, null, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getTitle());
    }

    @Test
    void getAll_shouldPassSortOptionNameToRepository() {
        Page<Advertisement> page = new PageImpl<>(List.of());

        when(advertisementRepository.search(eq(AdvertisementStatus.APPROVED), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq("PRICE_ASC"), any(Pageable.class))).thenReturn(page);
        stubEmptyRatingSummary();

        advertisementService.getAll(null, null, null, null, null, SortOption.PRICE_ASC, Pageable.unpaged());

        verify(advertisementRepository, times(1)).search(eq(AdvertisementStatus.APPROVED), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq("PRICE_ASC"), any(Pageable.class));
    }

    @Test
    void getAll_twoArgOverload_shouldDelegateWithApprovedStatusAndUnpagedPageable() {
        Page<Advertisement> page = new PageImpl<>(List.of());

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        when(advertisementRepository.search(eq(AdvertisementStatus.APPROVED), isNull(), eq(List.of(1L)), eq(2L),
                isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(page);
        stubEmptyRatingSummary();

        Page<AdvertisementResponse> result = advertisementService.getAll(1L, 2L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_shouldIncludeSubCategoryIds_whenParentCategoryIsSelected() {
        // Selecting a parent category should also surface ads posted under its
        // sub-categories, at any depth.
        Category parent = category();
        Category child = Category.builder().id(2L).name("Laptops").active(true)
                .children(new ArrayList<>()).build();
        Category grandchild = Category.builder().id(3L).name("Gaming Laptops").active(true)
                .children(new ArrayList<>()).build();
        parent.setChildren(new ArrayList<>(List.of(child)));
        child.setChildren(new ArrayList<>(List.of(grandchild)));

        Page<Advertisement> page = new PageImpl<>(List.of());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(advertisementRepository.search(eq(AdvertisementStatus.APPROVED), isNull(),
                eq(List.of(1L, 2L, 3L)), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);
        stubEmptyRatingSummary();

        advertisementService.getAll(null, 1L, null, null, null, null, Pageable.unpaged());

        verify(advertisementRepository, times(1)).search(eq(AdvertisementStatus.APPROVED), isNull(),
                eq(List.of(1L, 2L, 3L)), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    // ==================== getMyAdvertisements ====================

    @Test
    void getMyAdvertisements_shouldReturnEmptyList_whenUserHasNoAdvertisements() {
        User seller = seller();

        when(advertisementRepository.findBySellerId(1L)).thenReturn(List.of());
        stubEmptyRatingSummary();

        List<AdvertisementResponse> result = advertisementService.getMyAdvertisements(seller);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== update ====================

    @Test
    void update_shouldUpdateAdvertisementAndResetStatusToPending_whenUserIsOwner() {
        User seller = seller();
        Category category = category();
        City city = city();

        Advertisement ad = Advertisement.builder()
                .id(1L).title("Old Title").seller(seller).category(category).city(city)
                .status(AdvertisementStatus.APPROVED).images(new ArrayList<>()).build();

        AdvertisementRequest request = validRequest();
        request.setTitle("New Title");

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(advertisementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        stubEmptyRatingSummary();

        AdvertisementResponse response = advertisementService.update(1L, request, seller);

        assertEquals("New Title", response.getTitle());
        assertEquals(AdvertisementStatus.PENDING, response.getStatus());
    }

    @Test
    void update_shouldThrowUnauthorizedActionException_whenUserIsNotOwner() {
        User seller = seller();
        User stranger = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller).category(category()).city(city())
                .status(AdvertisementStatus.APPROVED).images(new ArrayList<>()).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> advertisementService.update(1L, validRequest(), stranger));

        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void update_shouldSyncImages_whenSomeAreKeptSomeRemovedAndOneAdded() {
        User seller = seller();
        Category category = category();
        City city = city();

        AdvertisementImage keptImage = AdvertisementImage.builder().id(1L).imageUrl("http://x/keep.jpg").build();
        AdvertisementImage removedImage = AdvertisementImage.builder().id(2L).imageUrl("http://x/remove.jpg").build();

        Advertisement ad = Advertisement.builder()
                .id(1L).title("Old").seller(seller).category(category).city(city)
                .status(AdvertisementStatus.APPROVED)
                .images(new ArrayList<>(List.of(keptImage, removedImage))).build();

        AdvertisementRequest request = validRequest();
        request.setImageUrls(List.of("http://x/keep.jpg", "http://x/new.jpg"));

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(advertisementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        stubEmptyRatingSummary();

        AdvertisementResponse response = advertisementService.update(1L, request, seller);

        List<String> urls = response.getImageUrls();
        assertEquals(2, urls.size());
        assertTrue(urls.contains("http://x/keep.jpg"));
        assertTrue(urls.contains("http://x/new.jpg"));
        assertFalse(urls.contains("http://x/remove.jpg"));
    }

    @Test
    void update_shouldKeepExistingImages_whenImageUrlsIsNull() {
        User seller = seller();
        Category category = category();
        City city = city();

        AdvertisementImage existingImage = AdvertisementImage.builder().id(1L).imageUrl("http://x/old.jpg").build();

        Advertisement ad = Advertisement.builder()
                .id(1L).title("Old").seller(seller).category(category).city(city)
                .status(AdvertisementStatus.APPROVED)
                .images(new ArrayList<>(List.of(existingImage))).build();

        AdvertisementRequest request = validRequest();
        request.setImageUrls(null);

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(advertisementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        stubEmptyRatingSummary();

        AdvertisementResponse response = advertisementService.update(1L, request, seller);

        assertEquals(1, response.getImageUrls().size());
        assertEquals("http://x/old.jpg", response.getImageUrls().get(0));
    }

    // ==================== delete (owner) ====================

    @Test
    void delete_shouldSetStatusToDeleted_whenUserIsOwner() {
        User seller = seller();
        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller).status(AdvertisementStatus.APPROVED).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        advertisementService.delete(1L, seller);

        assertEquals(AdvertisementStatus.DELETED, ad.getStatus());
        verify(advertisementRepository, times(1)).save(ad);
    }

    @Test
    void delete_shouldThrowUnauthorizedActionException_whenUserIsNotOwner() {
        User seller = seller();
        User stranger = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller).status(AdvertisementStatus.APPROVED).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> advertisementService.delete(1L, stranger));

        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void delete_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {
        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.delete(99L, seller()));
    }

    // ==================== markAsSold ====================

    @Test
    void markAsSold_shouldSetStatusToSold_whenAdvertisementIsApprovedAndUserIsOwner() {
        User seller = seller();
        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller).category(category()).city(city())
                .status(AdvertisementStatus.APPROVED).images(new ArrayList<>()).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(advertisementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        stubEmptyRatingSummary();

        AdvertisementResponse response = advertisementService.markAsSold(1L, seller);

        assertEquals(AdvertisementStatus.SOLD, response.getStatus());
    }

    @Test
    void markAsSold_shouldThrowUnauthorizedActionException_whenUserIsNotOwner() {
        User seller = seller();
        User stranger = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller).status(AdvertisementStatus.APPROVED).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> advertisementService.markAsSold(1L, stranger));

        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void markAsSold_shouldThrowInvalidAdvertisementStateException_whenAdvertisementIsNotApproved() {
        User seller = seller();
        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller).status(AdvertisementStatus.PENDING).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidAdvertisementStateException.class,
                () -> advertisementService.markAsSold(1L, seller));

        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void markAsSold_shouldThrowInvalidAdvertisementStateException_whenAdvertisementIsAlreadySold() {
        User seller = seller();
        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller).status(AdvertisementStatus.SOLD).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidAdvertisementStateException.class,
                () -> advertisementService.markAsSold(1L, seller));
    }

    // ==================== admin: getPendingAdvertisements ====================

    @Test
    void getPendingAdvertisements_shouldReturnPendingAdvertisements_whenSomeExist() {
        Advertisement ad = Advertisement.builder()
                .id(1L).title("Laptop").seller(seller()).category(category()).city(city())
                .status(AdvertisementStatus.PENDING).images(new ArrayList<>()).build();

        when(advertisementRepository.findByStatus(AdvertisementStatus.PENDING)).thenReturn(List.of(ad));
        stubEmptyRatingSummary();

        List<AdminAdvertisementResponse> result = advertisementService.getPendingAdvertisements();

        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getTitle());
        assertEquals(1L, result.get(0).getSellerId());
    }

    // ==================== admin: approve ====================

    @Test
    void approve_shouldSetStatusToApproved_whenAdvertisementIsPending() {
        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller()).category(category()).city(city())
                .status(AdvertisementStatus.PENDING).rejectionReason("old reason")
                .images(new ArrayList<>()).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(advertisementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        stubEmptyRatingSummary();

        AdminAdvertisementResponse response = advertisementService.approve(1L);

        assertEquals(AdvertisementStatus.APPROVED, response.getStatus());
        assertNull(response.getRejectionReason());
    }

    @Test
    void approve_shouldThrowInvalidAdvertisementStateException_whenNotPending() {
        Advertisement ad = Advertisement.builder().id(1L).status(AdvertisementStatus.APPROVED).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidAdvertisementStateException.class,
                () -> advertisementService.approve(1L));
    }

    @Test
    void approve_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {
        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.approve(99L));
    }

    // ==================== admin: reject ====================

    @Test
    void reject_shouldSetStatusToRejectedWithReason_whenAdvertisementIsPending() {
        Advertisement ad = Advertisement.builder()
                .id(1L).seller(seller()).category(category()).city(city())
                .status(AdvertisementStatus.PENDING).images(new ArrayList<>()).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(advertisementRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        stubEmptyRatingSummary();

        AdminAdvertisementResponse response = advertisementService.reject(1L, "Inappropriate content");

        assertEquals(AdvertisementStatus.REJECTED, response.getStatus());
        assertEquals("Inappropriate content", response.getRejectionReason());
    }

    @Test
    void reject_shouldThrowInvalidAdvertisementStateException_whenNotPending() {
        Advertisement ad = Advertisement.builder().id(1L).status(AdvertisementStatus.APPROVED).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidAdvertisementStateException.class,
                () -> advertisementService.reject(1L, "reason"));
    }

    // ==================== admin: adminDelete ====================

    @Test
    void adminDelete_shouldSetStatusToDeleted_whenNotAlreadyDeleted() {
        Advertisement ad = Advertisement.builder().id(1L).status(AdvertisementStatus.APPROVED).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        advertisementService.adminDelete(1L);

        assertEquals(AdvertisementStatus.DELETED, ad.getStatus());
        verify(advertisementRepository, times(1)).save(ad);
    }

    @Test
    void adminDelete_shouldThrowInvalidAdvertisementStateException_whenAlreadyDeleted() {
        Advertisement ad = Advertisement.builder().id(1L).status(AdvertisementStatus.DELETED).build();

        when(advertisementRepository.findById(1L)).thenReturn(Optional.of(ad));

        assertThrows(InvalidAdvertisementStateException.class,
                () -> advertisementService.adminDelete(1L));

        verify(advertisementRepository, never()).save(any());
    }

    @Test
    void adminDelete_shouldThrowAdvertisementNotFoundException_whenAdvertisementDoesNotExist() {
        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> advertisementService.adminDelete(99L));
    }
}
