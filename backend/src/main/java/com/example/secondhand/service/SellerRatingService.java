package com.example.secondhand.service;

import com.example.secondhand.dto.SellerRatingRequest;
import com.example.secondhand.dto.SellerRatingSummary;
import com.example.secondhand.dto.response.SellerRatingResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.RatingAlreadyExistsException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.Advertisement;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.model.SellerRating;
import com.example.secondhand.model.User;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.SellerRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h2>SellerRatingService</h2>
 * <p>
 * سرویس مسئول مدیریت <b>امتیازدهی به فروشندگان</b> بر اساس آگهی‌های خریداری‌شده.
 * هر امتیاز به یک آگهی مشخص تعلق دارد و در واقع بازخوردی است که خریدار درباره‌ی
 * فروشنده‌ی آن آگهی ثبت می‌کند.
 * </p>
 * <ul>
 *   <li>ثبت امتیاز برای یک آگهی، با جلوگیری از امتیازدهی به آگهی خود و امتیازدهی تکراری</li>
 *   <li>دریافت لیست کامل امتیازهای یک فروشنده</li>
 *   <li>محاسبه‌ی تعداد و میانگین امتیازهای یک فروشنده</li>
 *   <li>دریافت خلاصه‌ی امتیاز چندین فروشنده به‌صورت یک‌جا (برای نمایش در لیست آگهی‌ها)</li>
 * </ul>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.SellerRating
 */
@Service
@RequiredArgsConstructor
public class SellerRatingService {

    private final SellerRatingRepository sellerRatingRepository;
    private final AdvertisementRepository advertisementRepository;

    /**
     * ثبت یک امتیاز جدید برای فروشنده‌ی یک آگهی، توسط کاربر جاری.
     * <p>
     * آگهی باید در وضعیت {@code APPROVED} یا {@code SOLD} باشد، کاربر جاری
     * نباید فروشنده‌ی همان آگهی باشد و نباید قبلاً به همین آگهی امتیاز داده باشد.
     * </p>
     *
     * @param advertisementId شناسه آگهی‌ای که امتیاز برای فروشنده‌ی آن ثبت می‌شود
     * @param request         اطلاعات امتیاز شامل مقدار امتیاز و توضیح (اختیاری)
     * @param currentUser     کاربری که در نقش خریدار امتیاز را ثبت می‌کند
     * @return {@link SellerRatingResponse} حاوی اطلاعات امتیاز ثبت‌شده
     * @throws AdvertisementNotFoundException در صورتی که آگهی یافت نشود یا در
     *         وضعیتی نباشد که امکان امتیازدهی به آن وجود داشته باشد
     * @throws UnauthorizedActionException در صورتی که کاربر جاری بخواهد به
     *         آگهی خودش امتیاز دهد
     * @throws RatingAlreadyExistsException در صورتی که کاربر جاری قبلاً به
     *         این آگهی امتیاز داده باشد
     */
    @Transactional
    public SellerRatingResponse rateAdvertisement(Long advertisementId, SellerRatingRequest request, User currentUser) {

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (advertisement.getStatus() != AdvertisementStatus.APPROVED
                && advertisement.getStatus() != AdvertisementStatus.SOLD) {
            throw new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد");
        }

        if (advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("شما نمی‌توانید به آگهی خودتان امتیاز دهید");
        }

        if (sellerRatingRepository.existsByBuyerIdAndAdvertisementId(currentUser.getId(), advertisementId)) {
            throw new RatingAlreadyExistsException("شما قبلاً به این آگهی امتیاز داده‌اید");
        }

        SellerRating sellerRating = SellerRating.builder().buyer(currentUser)
                .advertisement(advertisement).rating(request.getRating()).comment(request.getComment()).build();

        return mapToResponse(sellerRatingRepository.save(sellerRating));

    }

    /**
     * دریافت لیست تمام امتیازهای ثبت‌شده برای یک فروشنده مشخص.
     *
     * @param sellerId شناسه فروشنده‌ای که امتیازهای او باید دریافت شود
     * @return لیستی از {@link SellerRatingResponse} مرتبط با آگهی‌های این فروشنده
     */
    @Transactional(readOnly = true)
    public List<SellerRatingResponse> getSellerRatings(Long sellerId) {
        return sellerRatingRepository.findByAdvertisementSellerId(sellerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * محاسبه‌ی تعداد کل امتیازهای ثبت‌شده برای یک فروشنده مشخص.
     *
     * @param sellerId شناسه فروشنده مورد نظر
     * @return تعداد امتیازهای ثبت‌شده برای این فروشنده
     */
    @Transactional(readOnly = true)
    public Long getSellerRatingCount(Long sellerId) {
        return (long) sellerRatingRepository.findByAdvertisementSellerId(sellerId).size();
    }

    /**
     * محاسبه‌ی میانگین امتیازهای ثبت‌شده برای یک فروشنده مشخص.
     *
     * @param sellerId شناسه فروشنده مورد نظر
     * @return میانگین امتیازهای این فروشنده؛ در صورت نداشتن هیچ امتیازی، مقدار {@code 0.0}
     */
    @Transactional(readOnly = true)
    public Double getSellerAverageRating(Long sellerId) {

        List<SellerRating> ratings = sellerRatingRepository.findByAdvertisementSellerId(sellerId);
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream()
                .mapToInt(SellerRating::getRating)
                .average()
                .orElse(0.0);

    }


    /**
     * دریافت خلاصه‌ی امتیاز (میانگین و تعداد) چندین فروشنده به‌صورت یک‌جا.
     * <p>
     * این متد برای جلوگیری از اجرای جداگانه‌ی کوئری به‌ازای هر فروشنده (مثلاً
     * هنگام نمایش لیستی از آگهی‌های متعلق به فروشندگان مختلف) استفاده می‌شود.
     * برای فروشندگانی که هیچ امتیازی ندارند، مقدار {@link SellerRatingSummary#EMPTY}
     * در نظر گرفته می‌شود.
     * </p>
     *
     * @param sellerIds لیست شناسه‌های فروشندگانی که خلاصه امتیاز آن‌ها باید دریافت شود
     * @return نگاشتی از شناسه فروشنده به {@link SellerRatingSummary} او؛ در صورت
     *         {@code null} یا خالی بودن ورودی، یک نگاشت خالی بازگردانده می‌شود
     */
    @Transactional(readOnly = true)
    public Map<Long, SellerRatingSummary> getRatingSummariesForSellers(List<Long> sellerIds) {
        Map<Long, SellerRatingSummary> summaries = new HashMap<>();

        if (sellerIds == null || sellerIds.isEmpty()) {
            return summaries;
        }

        List<Long> distinctSellerIds = sellerIds.stream().distinct().toList();

        for (Long sellerId : distinctSellerIds) {
            summaries.put(sellerId, SellerRatingSummary.EMPTY);
        }

        sellerRatingRepository.findRatingAggregatesBySellerIds(distinctSellerIds)
                .forEach(aggregate -> summaries.put(
                        aggregate.getSellerId(),
                        new SellerRatingSummary(aggregate.getAverageRating(), aggregate.getRatingCount())
                ));

        return summaries;
    }

    /**
     * تبدیل شیء {@link SellerRating} به DTO خروجی {@link SellerRatingResponse}.
     *
     * @param sellerRating موجودیت امتیاز فروشنده
     * @return شیء {@link SellerRatingResponse} متناظر با امتیاز
     */
    private SellerRatingResponse mapToResponse(SellerRating sellerRating) {
        return SellerRatingResponse.builder()
                .id(sellerRating.getId())
                .buyerId(sellerRating.getBuyer().getId())
                .buyerUsername(sellerRating.getBuyer().getUsername())
                .sellerId(sellerRating.getAdvertisement().getSeller().getId())
                .sellerUsername(sellerRating.getAdvertisement().getSeller().getUsername())
                .advertisementId(sellerRating.getAdvertisement().getId())
                .advertisementTitle(sellerRating.getAdvertisement().getTitle())
                .rating(sellerRating.getRating())
                .comment(sellerRating.getComment())
                .createdAt(sellerRating.getCreatedAt())
                .build();
    }

}
