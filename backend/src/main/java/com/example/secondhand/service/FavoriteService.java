package com.example.secondhand.service;

import com.example.secondhand.dto.response.FavoriteResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.FavoriteAlreadyExistsException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.Advertisement;
import com.example.secondhand.model.AdvertisementImage;
import com.example.secondhand.model.Favorite;
import com.example.secondhand.model.User;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final AdvertisementRepository advertisementRepository;

    public FavoriteResponse addFavorite(Long advertisementId, User currentUser) {

        if (currentUser == null) {
            throw new UnauthorizedActionException("برای افزودن به علاقه‌مندی‌ها باید وارد حساب کاربری شوید");
        }

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        boolean favoriteAlreadyExist = favoriteRepository.existsByUserIdAndAdvertisementId(currentUser.getId(), advertisementId);
        if (favoriteAlreadyExist) {
            throw new FavoriteAlreadyExistsException("این آگهی قبلاً به علاقه‌مندی‌های شما اضافه شده است");
        }

        Favorite favorite = Favorite.builder().advertisement(advertisement).user(currentUser).build();

        return mapToResponse(favoriteRepository.save(favorite));

    }

    private FavoriteResponse mapToResponse(Favorite favorite) {
        Advertisement ad = favorite.getAdvertisement();
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .advertisementId(ad.getId())
                .advertisementTitle(ad.getTitle())
                .advertisementDescription(ad.getDescription())
                .price(ad.getPrice())
                .cityName(ad.getCity().getName())
                .categoryName(ad.getCategory().getName())
                .advertisementStatus(ad.getStatus())
                .imageUrls(ad.getImages().stream().map(AdvertisementImage::getImageUrl).toList())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
