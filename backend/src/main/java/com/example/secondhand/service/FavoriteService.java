package com.example.secondhand.service;

import com.example.secondhand.dto.response.FavoriteResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.FavoriteAlreadyExistsException;
import com.example.secondhand.exception.FavoriteNotFoundException;
import com.example.secondhand.model.*;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <h2>FavoriteService</h2>
 * <p>
 * سرویس مسئول مدیریت <b>علاقه‌مندی‌های (Favorites)</b> کاربران نسبت به آگهی‌ها.
 * این کلاس امکان افزودن، حذف و مشاهده‌ی لیست آگهی‌های موردعلاقه‌ی هر کاربر را
 * فراهم می‌کند.
 * </p>
 * <ul>
 *   <li>افزودن یک آگهی به لیست علاقه‌مندی‌ها، با بررسی وضعیت آگهی و جلوگیری از افزودن تکراری</li>
 *   <li>حذف یک آگهی از لیست علاقه‌مندی‌ها</li>
 *   <li>دریافت لیست کامل علاقه‌مندی‌های کاربر جاری</li>
 * </ul>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.Favorite
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final AdvertisementRepository advertisementRepository;

    /**
     * افزودن یک آگهی به لیست علاقه‌مندی‌های کاربر جاری.
     * <p>
     * فقط آگهی‌های در وضعیت {@code APPROVED} یا {@code SOLD} قابل افزودن به
     * علاقه‌مندی‌ها هستند و هر کاربر فقط می‌تواند یک بار یک آگهی مشخص را به
     * علاقه‌مندی‌های خود اضافه کند.
     * </p>
     *
     * @param advertisementId شناسه آگهی‌ای که باید به علاقه‌مندی‌ها اضافه شود
     * @param currentUser     کاربری که آگهی را به علاقه‌مندی‌های خود اضافه می‌کند
     * @return {@link FavoriteResponse} حاوی اطلاعات علاقه‌مندی تازه‌ثبت‌شده
     * @throws AdvertisementNotFoundException در صورتی که آگهی یافت نشود یا در
     *         وضعیتی نباشد که امکان افزودن آن به علاقه‌مندی‌ها وجود داشته باشد
     * @throws FavoriteAlreadyExistsException در صورتی که این آگهی قبلاً به
     *         علاقه‌مندی‌های کاربر جاری اضافه شده باشد
     */
    @Transactional
    public FavoriteResponse addFavorite(Long advertisementId, User currentUser) {

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (advertisement.getStatus() != AdvertisementStatus.APPROVED
                && advertisement.getStatus() != AdvertisementStatus.SOLD) {
            throw new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد");
        }

        boolean favoriteAlreadyExist = favoriteRepository.existsByUserIdAndAdvertisementId(currentUser.getId(), advertisementId);
        if (favoriteAlreadyExist) {
            throw new FavoriteAlreadyExistsException("این آگهی قبلاً به علاقه‌مندی‌های شما اضافه شده است");
        }

        Favorite favorite = Favorite.builder().advertisement(advertisement).user(currentUser).build();

        return mapToResponse(favoriteRepository.save(favorite));

    }

    /**
     * حذف یک آگهی از لیست علاقه‌مندی‌های کاربر جاری.
     *
     * @param advertisementId شناسه آگهی‌ای که باید از علاقه‌مندی‌ها حذف شود
     * @param currentUser     کاربری که آگهی را از علاقه‌مندی‌های خود حذف می‌کند
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     * @throws FavoriteNotFoundException در صورتی که این آگهی در علاقه‌مندی‌های
     *         کاربر جاری وجود نداشته باشد
     */
    @Transactional
    public void removeFavorite(Long advertisementId, User currentUser) {

        if (!advertisementRepository.existsById(advertisementId)) {
            throw new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد");
        }

        if (!favoriteRepository.existsByUserIdAndAdvertisementId(currentUser.getId(), advertisementId)) {
            throw new FavoriteNotFoundException("این آگهی در علاقه‌مندی‌های شما وجود ندارد");
        }

        favoriteRepository.deleteByUserIdAndAdvertisementId(currentUser.getId(), advertisementId);

    }

    /**
     * دریافت لیست تمام آگهی‌های موردعلاقه‌ی کاربر جاری.
     *
     * @param currentUser کاربری که لیست علاقه‌مندی‌های او باید دریافت شود
     * @return لیستی از {@link FavoriteResponse} حاوی اطلاعات آگهی‌های موردعلاقه
     */
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(User currentUser) {

        return favoriteRepository.findByUserId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * تبدیل شیء {@link Favorite} به DTO خروجی {@link FavoriteResponse}، به‌همراه
     * اطلاعات خلاصه‌ی آگهی مرتبط (عنوان، توضیحات، قیمت، شهر، دسته‌بندی، وضعیت و تصاویر).
     *
     * @param favorite موجودیت علاقه‌مندی
     * @return شیء {@link FavoriteResponse} متناظر با علاقه‌مندی
     */
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
