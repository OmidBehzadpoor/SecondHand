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

/**
 * <h2>AdvertisementService</h2>
 * <p>
 * سرویس اصلی مدیریت <b>آگهی‌ها</b> در سامانه، شامل عملیات ایجاد، ویرایش، حذف،
 * جست‌وجو و مدیریت وضعیت آگهی‌ها (تایید/رد توسط ادمین، علامت‌گذاری به‌عنوان فروخته‌شده).
 * </p>
 * <ul>
 *   <li><b>عملیات کاربر عادی</b>: ایجاد آگهی، مشاهده آگهی‌های خودش، ویرایش، حذف و
 *       علامت‌گذاری به‌عنوان فروخته‌شده</li>
 *   <li><b>جست‌وجو و فهرست عمومی</b>: فیلتر بر اساس کلمه کلیدی، دسته‌بندی (به‌همراه
 *       زیردسته‌ها)، شهر، محدوده قیمت و ترتیب نمایش</li>
 *   <li><b>عملیات ادمین</b>: مشاهده آگهی‌های در انتظار بررسی، تایید، رد و حذف آگهی‌ها</li>
 * </ul>
 * <p>
 * این سرویس همچنین از {@link SellerRatingService} برای افزودن خلاصه امتیاز فروشنده
 * به پاسخ‌های خروجی استفاده می‌کند.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.Advertisement
 * @see com.example.secondhand.service.SellerRatingService
 */
@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final SellerRatingService sellerRatingService;

    /**
     * ایجاد یک آگهی جدید برای کاربر جاری.
     *
     * @param request     اطلاعات آگهی شامل عنوان، توضیحات، قیمت، دسته‌بندی، شهر و آدرس تصاویر
     * @param currentUser کاربری که آگهی را ایجاد می‌کند (فروشنده)
     * @return {@link AdvertisementResponse} حاوی اطلاعات آگهی تازه‌ایجادشده
     * @throws CategoryNotFoundException در صورتی که دسته‌بندی انتخاب‌شده یافت نشود
     * @throws CityNotFoundException در صورتی که شهر انتخاب‌شده یافت نشود
     */
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

    /**
     * دریافت یک آگهی بر اساس شناسه، با درنظرگرفتن سطح دسترسی کاربر درخواست‌دهنده.
     * <p>
     * آگهی برای مالک آن و برای ادمین همیشه قابل مشاهده است. برای سایر کاربران
     * (یا کاربران مهمان) فقط در صورتی که آگهی در وضعیت {@code APPROVED} یا
     * {@code SOLD} باشد قابل مشاهده خواهد بود.
     * </p>
     *
     * @param id          شناسه آگهی مورد نظر
     * @param currentUser کاربر جاری (می‌تواند {@code null} باشد برای کاربر مهمان)
     * @return {@link AdvertisementResponse} حاوی اطلاعات کامل آگهی
     * @throws AdvertisementNotFoundException در صورتی که آگهی یافت نشود یا برای
     *         کاربر درخواست‌دهنده قابل مشاهده نباشد
     */
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

    /**
     * دریافت لیست صفحه‌بندی‌نشده آگهی‌ها با فیلتر ساده بر اساس دسته‌بندی و شهر.
     * <p>
     * این متد یک نسخه ساده‌شده از {@link #getAll(String, Long, Long, Long, Long, SortOption, Pageable)}
     * است که بدون کلمه کلیدی، محدوده قیمت و مرتب‌سازی خاص، و بدون صفحه‌بندی عمل می‌کند.
     * </p>
     *
     * @param categoryId شناسه دسته‌بندی برای فیلتر (اختیاری، می‌تواند {@code null} باشد)
     * @param cityId     شناسه شهر برای فیلتر (اختیاری، می‌تواند {@code null} باشد)
     * @return صفحه‌ای از {@link AdvertisementResponse} مطابق با فیلترهای داده‌شده
     */
    @Transactional(readOnly = true)
    public Page<AdvertisementResponse> getAll(Long categoryId, Long cityId) {
        return getAll(null, categoryId, cityId, null, null, null, Pageable.unpaged());
    }

    /**
     * جست‌وجو و دریافت لیست صفحه‌بندی‌شده آگهی‌های تاییدشده، بر اساس فیلترهای متنوع.
     * <p>
     * در صورت انتخاب یک دسته‌بندی، آگهی‌های تمام زیردسته‌های آن دسته (در هر عمقی)
     * نیز در نتیجه جست‌وجو لحاظ می‌شوند.
     * </p>
     *
     * @param keyword    کلمه کلیدی برای جست‌وجو در عنوان/توضیحات (اختیاری)
     * @param categoryId شناسه دسته‌بندی برای فیلتر (اختیاری)
     * @param cityId     شناسه شهر برای فیلتر (اختیاری)
     * @param minPrice   حداقل قیمت مجاز برای فیلتر (اختیاری)
     * @param maxPrice   حداکثر قیمت مجاز برای فیلتر (اختیاری)
     * @param sortBy     نوع مرتب‌سازی نتایج (اختیاری)
     * @param pageable   اطلاعات صفحه‌بندی (شماره صفحه و اندازه صفحه)
     * @return صفحه‌ای از {@link AdvertisementResponse} مطابق با فیلترهای داده‌شده
     * @throws InvalidAdvertisementStateException در صورتی که {@code minPrice} بزرگ‌تر از {@code maxPrice} باشد
     */
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
    /**
     * دریافت لیست شناسه‌ی یک دسته‌بندی به‌همراه شناسه‌ی تمام زیردسته‌های آن.
     *
     * @param categoryId شناسه دسته‌بندی مورد نظر؛ در صورت {@code null} بودن، مقدار
     *                   {@code null} بازگردانده می‌شود (یعنی بدون فیلتر دسته‌بندی)
     * @return لیست شناسه‌های دسته‌بندی و زیردسته‌های آن، یا {@code null} اگر
     *         {@code categoryId} ورودی {@code null} باشد
     */
    private List<Long> resolveCategoryAndDescendantIds(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .map(this::collectCategoryAndDescendantIds)
                .orElse(List.of(categoryId));
    }

    /**
     * جمع‌آوری بازگشتی شناسه‌ی یک دسته‌بندی و تمام زیردسته‌های آن در هر عمقی.
     *
     * @param category دسته‌بندی ریشه برای شروع جمع‌آوری
     * @return لیستی از شناسه‌های دسته‌بندی داده‌شده و تمام فرزندانش
     */
    private List<Long> collectCategoryAndDescendantIds(Category category) {
        List<Long> ids = new java.util.ArrayList<>();
        ids.add(category.getId());
        for (Category child : category.getChildren()) {
            ids.addAll(collectCategoryAndDescendantIds(child));
        }
        return ids;
    }


    /**
     * دریافت لیست تمام آگهی‌های ثبت‌شده توسط کاربر جاری (بدون فیلتر وضعیت).
     *
     * @param currentUser کاربری که آگهی‌های او باید بازگردانده شود
     * @return لیستی از {@link AdvertisementResponse} متعلق به کاربر جاری
     */
    public List<AdvertisementResponse> getMyAdvertisements(User currentUser) {
        List<Advertisement> advertisements = advertisementRepository.findBySellerId(currentUser.getId());

        Map<Long, SellerRatingSummary> ratingSummaries = ratingSummariesFor(advertisements);

        return advertisements.stream()
                .map(advertisement -> mapToResponse(advertisement, ratingSummaries))
                .toList();
    }

    /**
     * ویرایش یک آگهی موجود توسط مالک آن.
     * <p>
     * پس از ویرایش، وضعیت آگهی به‌طور خودکار به {@code PENDING} تغییر می‌کند تا
     * دوباره توسط ادمین بررسی شود. در صورت ارسال لیست تصاویر جدید، تصاویر آگهی
     * با استفاده از {@link #syncImages} همگام‌سازی می‌شوند.
     * </p>
     *
     * @param id          شناسه آگهی‌ای که باید ویرایش شود
     * @param request     اطلاعات جدید آگهی
     * @param currentUser کاربر جاری که درخواست ویرایش را ارسال کرده است
     * @return {@link AdvertisementResponse} حاوی اطلاعات به‌روزشده آگهی
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     * @throws UnauthorizedActionException در صورتی که کاربر جاری مالک آگهی نباشد
     * @throws CategoryNotFoundException در صورتی که دسته‌بندی انتخاب‌شده یافت نشود
     * @throws CityNotFoundException در صورتی که شهر انتخاب‌شده یافت نشود
     */
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

    /**
     * حذف نرم (Soft Delete) یک آگهی توسط مالک آن.
     * <p>
     * این متد آگهی را فیزیکی حذف نمی‌کند؛ بلکه وضعیت آن را به {@code DELETED} تغییر می‌دهد.
     * </p>
     *
     * @param id          شناسه آگهی‌ای که باید حذف شود
     * @param currentUser کاربر جاری که درخواست حذف را ارسال کرده است
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     * @throws UnauthorizedActionException در صورتی که کاربر جاری مالک آگهی نباشد
     * @throws InvalidAdvertisementStateException در صورتی که آگهی از قبل حذف شده باشد
     */
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

    /**
     * علامت‌گذاری یک آگهی تاییدشده به‌عنوان <b>فروخته‌شده</b>، توسط مالک آن.
     *
     * @param id          شناسه آگهی‌ای که باید فروخته‌شده علامت زده شود
     * @param currentUser کاربر جاری که درخواست را ارسال کرده است
     * @return {@link AdvertisementResponse} حاوی اطلاعات به‌روزشده آگهی
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     * @throws UnauthorizedActionException در صورتی که کاربر جاری مالک آگهی نباشد
     * @throws InvalidAdvertisementStateException در صورتی که آگهی در وضعیت {@code APPROVED} نباشد
     */
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

    /**
     * افزودن یک لیست تصویر جدید به آگهی (برای زمان ایجاد آگهی).
     *
     * @param advertisement آگهی مقصد برای افزودن تصاویر
     * @param imageUrls     لیست آدرس تصاویر برای افزودن؛ در صورت {@code null} یا خالی
     *                      بودن، هیچ عملیاتی انجام نمی‌شود
     */
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

    /**
     * همگام‌سازی لیست تصاویر یک آگهی با لیست جدید آدرس‌های تصاویر (برای زمان ویرایش آگهی).
     * <p>
     * تصاویری که در لیست جدید وجود ندارند حذف می‌شوند و آدرس‌هایی که تصویر متناظر
     * موجود ندارند به‌عنوان تصویر جدید اضافه می‌شوند.
     * </p>
     *
     * @param advertisement       آگهی‌ای که تصاویر آن باید همگام‌سازی شود
     * @param requestedImageUrls  لیست نهایی آدرس تصاویر مدنظر کاربر
     */
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

    /**
     * تبدیل شیء {@link Advertisement} به DTO خروجی عمومی {@link AdvertisementResponse}،
     * به‌همراه خلاصه امتیاز فروشنده.
     *
     * @param advertisement   موجودیت آگهی
     * @param ratingSummaries نگاشتی از شناسه فروشنده به خلاصه امتیاز او
     * @return شیء {@link AdvertisementResponse} متناظر با آگهی
     */
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
                .ownerName(advertisement.getSeller().getName())   // ← این خط را اضافه کن
                .sellerAverageRating(ratingSummary.getAverageRating())
                .sellerRatingCount(ratingSummary.getRatingCount())
                .imageUrls(advertisement.getImages().stream().map(AdvertisementImage::getImageUrl).toList())
                .createdAt(advertisement.getCreatedAt())
                .build();
    }

    /**
     * دریافت لیست تمام آگهی‌های در انتظار بررسی، مخصوص پنل ادمین.
     *
     * @return لیستی از {@link AdminAdvertisementResponse} با وضعیت {@code PENDING}
     */
    @Transactional(readOnly = true)
    public List<AdminAdvertisementResponse> getPendingAdvertisements() {
        List<Advertisement> advertisements = advertisementRepository.findByStatus(AdvertisementStatus.PENDING);

        Map<Long, SellerRatingSummary> ratingSummaries = ratingSummariesFor(advertisements);

        return advertisements.stream()
                .map(advertisement -> mapToAdminResponse(advertisement, ratingSummaries))
                .toList();
    }

    /**
     * تایید یک آگهی در انتظار بررسی، توسط ادمین.
     *
     * @param id شناسه آگهی‌ای که باید تایید شود
     * @return {@link AdminAdvertisementResponse} حاوی اطلاعات به‌روزشده آگهی پس از تایید
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     * @throws InvalidAdvertisementStateException در صورتی که آگهی در وضعیت {@code PENDING} نباشد
     */
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

    /**
     * رد یک آگهی در انتظار بررسی، توسط ادمین، به‌همراه ذکر دلیل رد.
     *
     * @param id     شناسه آگهی‌ای که باید رد شود
     * @param reason دلیل رد آگهی که برای فروشنده نمایش داده می‌شود
     * @return {@link AdminAdvertisementResponse} حاوی اطلاعات به‌روزشده آگهی پس از رد
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     * @throws InvalidAdvertisementStateException در صورتی که آگهی در وضعیت {@code PENDING} نباشد
     */
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

    /**
     * حذف نرم (Soft Delete) یک آگهی توسط ادمین، بدون نیاز به بررسی مالکیت.
     *
     * @param id شناسه آگهی‌ای که باید حذف شود
     * @throws AdvertisementNotFoundException در صورتی که آگهی مورد نظر یافت نشود
     * @throws InvalidAdvertisementStateException در صورتی که آگهی از قبل حذف شده باشد
     */
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

    /**
     * دریافت لیست تمام آگهی‌های سامانه (بدون فیلتر وضعیت)، مخصوص پنل ادمین.
     *
     * @return لیستی از {@link AdminAdvertisementResponse} برای تمام آگهی‌ها
     */
    @Transactional(readOnly = true)
    public List<AdminAdvertisementResponse> getAllForAdmin() {
        List<Advertisement> advertisements = advertisementRepository.findAll();
        Map<Long, SellerRatingSummary> ratingSummaries = ratingSummariesFor(advertisements);
        return advertisements.stream()
                .map(ad -> mapToAdminResponse(ad, ratingSummaries))
                .toList();
    }

    /**
     * تبدیل شیء {@link Advertisement} به DTO خروجی مخصوص ادمین {@link AdminAdvertisementResponse}،
     * که شامل اطلاعات تماس فروشنده (شماره تماس و ایمیل) نیز می‌شود.
     *
     * @param advertisement   موجودیت آگهی
     * @param ratingSummaries نگاشتی از شناسه فروشنده به خلاصه امتیاز او
     * @return شیء {@link AdminAdvertisementResponse} متناظر با آگهی
     */
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

    /**
     * دریافت خلاصه امتیاز فروشندگان برای مجموعه‌ای از آگهی‌ها به‌صورت یک‌جا.
     *
     * @param advertisements لیست آگهی‌هایی که باید خلاصه امتیاز فروشندگان آن‌ها دریافت شود
     * @return نگاشتی از شناسه فروشنده به {@link SellerRatingSummary} او
     */
    private Map<Long, SellerRatingSummary> ratingSummariesFor(List<Advertisement> advertisements) {
        List<Long> sellerIds = advertisements.stream()
                .map(advertisement -> advertisement.getSeller().getId())
                .toList();

        return sellerRatingService.getRatingSummariesForSellers(sellerIds);
    }

    /**
     * دریافت خلاصه امتیاز یک فروشنده مشخص.
     *
     * @param sellerId شناسه فروشنده مورد نظر
     * @return نگاشتی حاوی خلاصه امتیاز فروشنده داده‌شده
     */
    private Map<Long, SellerRatingSummary> ratingSummaryFor(Long sellerId) {
        return sellerRatingService.getRatingSummariesForSellers(List.of(sellerId));
    }
}
