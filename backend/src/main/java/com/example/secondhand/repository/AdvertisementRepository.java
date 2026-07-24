package com.example.secondhand.repository;

import com.example.secondhand.model.Advertisement;
import com.example.secondhand.model.AdvertisementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <h2>AdvertisementRepository</h2>
 * <p>
 * ریپازیتوری Spring Data JPA برای موجودیت {@link Advertisement}. علاوه بر
 * عملیات پایه‌ی CRUD فراهم‌شده توسط {@link JpaRepository}، شامل متدهای
 * مشتق‌شده (Derived Query) برای فیلتر بر اساس وضعیت/فروشنده، و یک کوئری
 * سفارشی JPQL ({@link #search}) برای جست‌وجوی چندفیلتره‌ی صفحه‌بندی‌شده‌ی
 * آگهی‌ها است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.AdvertisementService
 */
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    /**
     * دریافت لیست آگهی‌ها با وضعیت مشخص.
     *
     * @param status وضعیتی که آگهی‌ها باید بر اساس آن فیلتر شوند
     * @return لیست آگهی‌های دارای وضعیت داده‌شده
     */
    List<Advertisement> findByStatus(AdvertisementStatus status);

    /**
     * دریافت لیست تمام آگهی‌های متعلق به یک فروشنده مشخص.
     *
     * @param sellerId شناسه فروشنده
     * @return لیست آگهی‌های ثبت‌شده توسط این فروشنده
     */
    List<Advertisement> findBySellerId(Long sellerId);

    /**
     * بررسی وجود حداقل یک آگهی متعلق به یک دسته‌بندی مشخص.
     *
     * @param categoryId شناسه دسته‌بندی
     * @return {@code true} در صورت وجود حداقل یک آگهی با این دسته‌بندی
     */
    boolean existsByCategoryId(Long categoryId);

    /**
     * بررسی وجود حداقل یک آگهی متعلق به یک شهر مشخص.
     *
     * @param cityId شناسه شهر
     * @return {@code true} در صورت وجود حداقل یک آگهی با این شهر
     */
    boolean existsByCityId(Long cityId);

    /**
     * شمارش تعداد آگهی‌های دارای وضعیت مشخص.
     *
     * @param status وضعیت مورد نظر
     * @return تعداد آگهی‌های با این وضعیت
     */
    long countByStatus(AdvertisementStatus status);

    /**
     * جست‌وجوی صفحه‌بندی‌شده‌ی آگهی‌ها با فیلترهای اختیاری متعدد و مرتب‌سازی
     * قابل تنظیم.
     * <p>
     * هر پارامتر فیلتر در صورت {@code null} بودن نادیده گرفته می‌شود (شرط
     * مربوطه در کوئری همیشه true ارزیابی می‌شود). کلمه کلیدی به‌صورت
     * غیرحساس به بزرگی/کوچکی حروف و با تطبیق زیررشته در عنوان یا توضیحات
     * آگهی جست‌وجو می‌شود. مرتب‌سازی بر اساس مقدار {@code sortBy} (یکی از
     * {@code PRICE_ASC}, {@code PRICE_DESC}, {@code OLDEST}) اعمال می‌شود؛
     * در غیر این صورت، پیش‌فرض بر اساس جدیدترین ({@code createdAt DESC})
     * مرتب می‌شود.
     * </p>
     *
     * @param status     وضعیتی که آگهی‌ها باید داشته باشند (معمولاً {@code APPROVED})
     * @param keyword    کلمه کلیدی جست‌وجو در عنوان/توضیحات (اختیاری)
     * @param categoryIds لیست شناسه‌های دسته‌بندی مجاز، شامل زیردسته‌ها (اختیاری)
     * @param cityId     شناسه شهر برای فیلتر (اختیاری)
     * @param minPrice   حداقل قیمت مجاز (اختیاری)
     * @param maxPrice   حداکثر قیمت مجاز (اختیاری)
     * @param sortBy     نوع مرتب‌سازی نتایج (اختیاری)
     * @param pageable   اطلاعات صفحه‌بندی
     * @return صفحه‌ای از {@link Advertisement} مطابق با فیلترها و مرتب‌سازی داده‌شده
     */
    @Query("""
    SELECT a FROM Advertisement a
    WHERE a.status = :status
      AND (:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:categoryIds IS NULL OR a.category.id IN :categoryIds)
      AND (:cityId IS NULL OR a.city.id = :cityId)
      AND (:minPrice IS NULL OR a.price >= :minPrice)
      AND (:maxPrice IS NULL OR a.price <= :maxPrice)
    ORDER BY
      CASE WHEN :sortBy = 'PRICE_ASC' THEN a.price END ASC,
      CASE WHEN :sortBy = 'PRICE_DESC' THEN a.price END DESC,
      CASE WHEN :sortBy = 'OLDEST' THEN a.createdAt END ASC,
      a.createdAt DESC
    """)
    Page<Advertisement> search(
            @Param("status") AdvertisementStatus status,
            @Param("keyword") String keyword,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("cityId") Long cityId,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("sortBy") String sortBy,
            Pageable pageable
    );

    /**
     * بررسی وجود حداقل یک آگهی متعلق به یکی از دسته‌بندی‌های داده‌شده که
     * وضعیت آن در لیست وضعیت‌های مشخص‌شده باشد.
     *
     * @param categoryIds لیست شناسه‌های دسته‌بندی (شامل یک دسته و زیردسته‌هایش)
     * @param statuses    لیست وضعیت‌های مجاز برای بررسی
     * @return {@code true} در صورت وجود حداقل یک آگهی مطابق با شرایط
     */
    boolean existsByCategoryIdInAndStatusIn(List<Long> categoryIds, List<AdvertisementStatus> statuses);

    /**
     * بررسی وجود حداقل یک آگهی متعلق به یک دسته‌بندی مشخص که وضعیت آن در
     * لیست وضعیت‌های مشخص‌شده باشد.
     *
     * @param categoryId شناسه دسته‌بندی
     * @param statuses   لیست وضعیت‌های مجاز برای بررسی
     * @return {@code true} در صورت وجود حداقل یک آگهی مطابق با شرایط
     */
    boolean existsByCategoryIdAndStatusIn(Long categoryId, List<AdvertisementStatus> statuses);
}
