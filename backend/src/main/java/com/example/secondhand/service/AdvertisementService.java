package com.example.secondhand.service;

import com.example.secondhand.dto.AdvertisementRequest;
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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;

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

        return mapToResponse(advertisementRepository.save(advertisement));
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

        return mapToResponse(advertisement);
    }

    public List<AdvertisementResponse> getAll(Long categoryId, Long cityId) {
        List<Advertisement> advertisements;

        if (categoryId != null && cityId != null) {
            advertisements = advertisementRepository.findByStatusAndCategoryIdAndCityId(
                    AdvertisementStatus.APPROVED, categoryId, cityId);
        } else if (categoryId != null) {
            advertisements = advertisementRepository.findByStatusAndCategoryId(AdvertisementStatus.APPROVED, categoryId);
        } else if (cityId != null) {
            advertisements = advertisementRepository.findByStatusAndCityId(AdvertisementStatus.APPROVED, cityId);
        } else {
            advertisements = advertisementRepository.findByStatus(AdvertisementStatus.APPROVED);
        }

        return advertisements.stream().map(this::mapToResponse).toList();
    }

    public List<AdvertisementResponse> getMyAdvertisements(User currentUser) {
        List<Advertisement> advertisements = advertisementRepository.findBySellerId(currentUser.getId());

        return advertisements.stream().map(this::mapToResponse).toList();
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

        advertisement.getImages().clear();
        addImages(advertisement, request.getImageUrls());

        return mapToResponse(advertisementRepository.save(advertisement));
    }

    public void delete(Long id, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (!advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما اجازه‌ی حذف این آگهی را ندارید");
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

        return mapToResponse(advertisementRepository.save(advertisement));
    }

    private void addImages(Advertisement advertisement, List<String> imageUrls) {
        if (imageUrls == null) {
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

    private AdvertisementResponse mapToResponse(Advertisement advertisement) {
        return AdvertisementResponse.builder()
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .description(advertisement.getDescription())
                .price(advertisement.getPrice())
                .cityName(advertisement.getCity().getName())
                .categoryName(advertisement.getCategory().getName())
                .status(advertisement.getStatus())
                .ownerId(advertisement.getSeller().getId())
                .ownerUsername(advertisement.getSeller().getUsername())
                .imageUrls(advertisement.getImages().stream().map(AdvertisementImage::getImageUrl).toList())
                .createdAt(advertisement.getCreatedAt())
                .build();
    }
}