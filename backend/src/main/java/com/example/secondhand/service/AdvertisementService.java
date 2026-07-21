package com.example.secondhand.service;

import com.example.secondhand.dto.AdvertisementRequest;
import com.example.secondhand.dto.SellerRatingSummary;
import com.example.secondhand.dto.response.AdminAdvertisementResponse;
import com.example.secondhand.dto.response.AdvertisementResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.CategoryNotFoundException;
import com.example.secondhand.exception.CityNotFoundException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.exception.InvalidAdvertisementStateException;
import com.example.secondhand.model.*;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CategoryRepository;
import com.example.secondhand.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final SellerRatingService sellerRatingService;

    public AdvertisementResponse create(AdvertisementRequest request, User currentUser) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی مورد نظر یافت نشد"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new CityNotFoundException("شهر مورد نظر یافت نشد"));

        Advertisement advertisement = Advertisement.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .city(city)
                .seller(currentUser)
                .build();

        addImages(advertisement, request.getImageUrls());

        return mapToResponse(advertisementRepository.save(advertisement), ratingSummaryFor(currentUser.getId()));
    }

    public AdvertisementResponse getById(Long id, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        boolean isOwner = currentUser != null
                && advertisement.getSeller().getId().equals(currentUser.getId());

        boolean isAdmin = currentUser != null && currentUser.getRole() == Role.ADMIN;

        boolean isPubliclyVisible = advertisement.getStatus() == AdvertisementStatus.APPROVED
                || advertisement.getStatus() == AdvertisementStatus.SOLD;

        if (!isOwner && !isAdmin && !isPubliclyVisible) {
            throw new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد");
        }

        return mapToResponse(advertisement, ratingSummaryFor(advertisement.getSeller().getId()));
    }

    @Transactional(readOnly = true)
    public Page<AdvertisementResponse> getAll(Long categoryId, Long cityId) {
        return getAll(null, categoryId, cityId, null, null, null, Pageable.unpaged());
    }

    @Transactional(readOnly = true)
    public Page<AdvertisementResponse> getAll(String keyword, Long categoryId, Long cityId,
                                              Long minPrice, Long maxPrice, SortOption sortBy,
                                              Pageable pageable) {

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new InvalidAdvertisementStateException("حداقل قیمت نمی‌تواند بیشتر از حداکثر قیمت باشد");
        }

        String sortByName = sortBy != null ? sortBy.name() : null;
        List<Long> categoryIds = resolveCategoryAndDescendantIds(categoryId);

        Page<Advertisement> advertisementsPage = advertisementRepository
                .search(AdvertisementStatus.APPROVED, keyword, categoryIds, cityId, minPrice, maxPrice, sortByName, pageable);

        Map<Long, SellerRatingSummary> ratingSummaries = ratingSummariesFor(advertisementsPage.getContent());

        return advertisementsPage.map(advertisement -> mapToResponse(advertisement, ratingSummaries));
    }

    // وقتی کاربر دسته‌ی والد را انتخاب می‌کند، آگهی‌های زیردسته‌هایش هم باید نمایش داده شوند؛
    // این متد شناسه‌ی خود دسته و همه‌ی زیردسته‌هایش را در هر عمقی جمع‌آوری می‌کند
    private List<Long> resolveCategoryAndDescendantIds(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .map(this::collectCategoryAndDescendantIds)
                .orElse(List.of(categoryId));
    }

    private List<Long> collectCategoryAndDescendantIds(Category category) {
        List<Long> ids = new java.util.ArrayList<>();
        ids.add(category.getId());
        for (Category child : category.getChildren()) {
            ids.addAll(collectCategoryAndDescendantIds(child));
        }
        return ids;
    }


    public List<AdvertisementResponse> getMyAdvertisements(User currentUser) {
        List<Advertisement> advertisements = advertisementRepository.findBySellerId(currentUser.getId());

        Map<Long, SellerRatingSummary> ratingSummaries = ratingSummariesFor(advertisements);

        return advertisements.stream()
                .map(advertisement -> mapToResponse(advertisement, ratingSummaries))
                .toList();
    }

    public AdvertisementResponse update(Long id, AdvertisementRequest request, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما اجازه‌ی ویرایش این آگهی را ندارید");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی مورد نظر یافت نشد"));

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new CityNotFoundException("شهر مورد نظر یافت نشد"));

        advertisement.setTitle(request.getTitle());
        advertisement.setDescription(request.getDescription());
        advertisement.setPrice(request.getPrice());
        advertisement.setCategory(category);
        advertisement.setCity(city);

        advertisement.setStatus(AdvertisementStatus.PENDING);

        if (request.getImageUrls() != null) {
            syncImages(advertisement, request.getImageUrls());
        }

        return mapToResponse(advertisementRepository.save(advertisement), ratingSummaryFor(currentUser.getId()));
    }

    public void delete(Long id, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما اجازه‌ی حذف این آگهی را ندارید");
        }

        if (advertisement.getStatus() == AdvertisementStatus.DELETED) {
            throw new InvalidAdvertisementStateException("این آگهی قبلاً حذف شده است");
        }

        advertisement.setStatus(AdvertisementStatus.DELETED);
        advertisementRepository.save(advertisement);
    }

    public AdvertisementResponse markAsSold(Long id, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما اجازه‌ی تغییر وضعیت این آگهی را ندارید");
        }

        if (advertisement.getStatus() != AdvertisementStatus.APPROVED) {
            throw new InvalidAdvertisementStateException("فقط آگهی‌های تاییدشده را می‌توان فروخته‌شده علامت زد");
        }

        advertisement.setStatus(AdvertisementStatus.SOLD);

        return mapToResponse(advertisementRepository.save(advertisement), ratingSummaryFor(currentUser.getId()));
    }

    private void addImages(Advertisement advertisement, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        List<AdvertisementImage> images = imageUrls.stream()
                .map(url -> AdvertisementImage.builder()
                        .imageUrl(url)
                        .advertisement(advertisement)
                        .build())
                .toList();

        advertisement.getImages().addAll(images);
    }

    private void syncImages(Advertisement advertisement, List<String> requestedImageUrls) {
        List<String> requested = requestedImageUrls != null ? requestedImageUrls : List.of();

        advertisement.getImages().removeIf(image -> !requested.contains(image.getImageUrl()));

        List<String> existingUrls = advertisement.getImages().stream()
                .map(AdvertisementImage::getImageUrl)
                .toList();

        List<AdvertisementImage> newImages = requested.stream()
                .filter(url -> !existingUrls.contains(url))
                .map(url -> AdvertisementImage.builder()
                        .imageUrl(url)
                        .advertisement(advertisement)
                        .build())
                .toList();

        advertisement.getImages().addAll(newImages);
    }

    private AdvertisementResponse mapToResponse(Advertisement advertisement, Map<Long, SellerRatingSummary> ratingSummaries) {
        SellerRatingSummary ratingSummary = ratingSummaries.getOrDefault(
                advertisement.getSeller().getId(), SellerRatingSummary.EMPTY);

        return AdvertisementResponse.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .description(advertisement.getDescription())
                .price(advertisement.getPrice())
                .cityName(advertisement.getCity().getName())
                .categoryName(advertisement.getCategory().getName())
                .status(advertisement.getStatus())
                .rejectionReason(advertisement.getRejectionReason())
                .ownerId(advertisement.getSeller().getId())
                .ownerUsername(advertisement.getSeller().getUsername())
                .sellerAverageRating(ratingSummary.getAverageRating())
                .sellerRatingCount(ratingSummary.getRatingCount())
                .imageUrls(advertisement.getImages().stream().map(AdvertisementImage::getImageUrl).toList())
                .createdAt(advertisement.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminAdvertisementResponse> getPendingAdvertisements() {
        List<Advertisement> advertisements = advertisementRepository.findByStatus(AdvertisementStatus.PENDING);

        Map<Long, SellerRatingSummary> ratingSummaries = ratingSummariesFor(advertisements);

        return advertisements.stream()
                .map(advertisement -> mapToAdminResponse(advertisement, ratingSummaries))
                .toList();
    }

    @Transactional
    public AdminAdvertisementResponse approve(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (advertisement.getStatus() != AdvertisementStatus.PENDING) {
            throw new InvalidAdvertisementStateException("فقط آگهی‌های در انتظار بررسی قابل تایید هستند");
        }

        advertisement.setStatus(AdvertisementStatus.APPROVED);
        advertisement.setRejectionReason(null);
        return mapToAdminResponse(advertisementRepository.save(advertisement),
                ratingSummaryFor(advertisement.getSeller().getId()));
    }

    @Transactional
    public AdminAdvertisementResponse reject(Long id, String reason) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (advertisement.getStatus() != AdvertisementStatus.PENDING) {
            throw new InvalidAdvertisementStateException("فقط آگهی‌های در انتظار بررسی قابل رد هستند");
        }

        advertisement.setStatus(AdvertisementStatus.REJECTED);
        advertisement.setRejectionReason(reason);
        return mapToAdminResponse(advertisementRepository.save(advertisement),
                ratingSummaryFor(advertisement.getSeller().getId()));
    }

    @Transactional
    public void adminDelete(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (advertisement.getStatus() == AdvertisementStatus.DELETED) {
            throw new InvalidAdvertisementStateException("این آگهی قبلاً حذف شده است");
        }

        advertisement.setStatus(AdvertisementStatus.DELETED);
        advertisementRepository.save(advertisement);
    }

    private AdminAdvertisementResponse mapToAdminResponse(Advertisement advertisement, Map<Long, SellerRatingSummary> ratingSummaries) {
        SellerRatingSummary ratingSummary = ratingSummaries.getOrDefault(
                advertisement.getSeller().getId(), SellerRatingSummary.EMPTY);

        return AdminAdvertisementResponse.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .description(advertisement.getDescription())
                .price(advertisement.getPrice())
                .categoryName(advertisement.getCategory().getName())
                .cityName(advertisement.getCity().getName())
                .status(advertisement.getStatus())
                .sellerId(advertisement.getSeller().getId())
                .sellerUsername(advertisement.getSeller().getUsername())
                .sellerName(advertisement.getSeller().getName())
                .sellerPhone(advertisement.getSeller().getPhone())
                .sellerEmail(advertisement.getSeller().getEmail())
                .sellerAverageRating(ratingSummary.getAverageRating())
                .sellerRatingCount(ratingSummary.getRatingCount())
                .imageUrls(advertisement.getImages().stream().map(AdvertisementImage::getImageUrl).toList())
                .rejectionReason(advertisement.getRejectionReason())
                .createdAt(advertisement.getCreatedAt())
                .build();
    }

    private Map<Long, SellerRatingSummary> ratingSummariesFor(List<Advertisement> advertisements) {
        List<Long> sellerIds = advertisements.stream()
                .map(advertisement -> advertisement.getSeller().getId())
                .toList();

        return sellerRatingService.getRatingSummariesForSellers(sellerIds);
    }

    private Map<Long, SellerRatingSummary> ratingSummaryFor(Long sellerId) {
        return sellerRatingService.getRatingSummariesForSellers(List.of(sellerId));
    }
}