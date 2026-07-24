package com.example.secondhand.repository;

import com.example.secondhand.model.SellerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <h2>SellerRatingRepository</h2>
 * <p>
 * ریپازیتوری Spring Data JPA برای موجودیت {@link SellerRating}. علاوه بر
 * عملیات پایه‌ی CRUD فراهم‌شده توسط {@link JpaRepository}، شامل متدهای
 * مشتق‌شده برای بررسی امتیاز تکراری و دریافت امتیازهای یک فروشنده، و یک
 * کوئری تجمیعی سفارشی ({@link #findRatingAggregatesBySellerIds}) برای
 * محاسبه‌ی یک‌جای میانگین و تعداد امتیاز چند فروشنده است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.SellerRatingService
 */
public interface SellerRatingRepository extends JpaRepository<SellerRating, Long> {

    /**
     * بررسی وجود امتیاز ثبت‌شده توسط یک خریدار مشخص برای یک آگهی مشخص.
     *
     * @param buyerId         شناسه خریدار
     * @param advertisementId شناسه آگهی
     * @return {@code true} در صورتی که این خریدار قبلاً به این آگهی امتیاز داده باشد
     */
    boolean existsByBuyerIdAndAdvertisementId(Long buyerId, Long advertisementId);

    /**
     * دریافت لیست تمام امتیازهای ثبت‌شده برای آگهی‌های یک فروشنده مشخص.
     *
     * @param sellerId شناسه فروشنده
     * @return لیست امتیازهای مرتبط با فروشنده‌ی داده‌شده
     */
    List<SellerRating> findByAdvertisementSellerId(Long sellerId);


    /**
     * محاسبه‌ی یک‌جای میانگین و تعداد امتیازهای چند فروشنده، برای جلوگیری
     * از اجرای کوئری جداگانه به‌ازای هر فروشنده.
     *
     * @param sellerIds لیست شناسه‌های فروشندگانی که آمار امتیاز آن‌ها باید محاسبه شود
     * @return لیستی از {@link SellerRatingAggregate} شامل شناسه، میانگین و تعداد
     *         امتیاز هر فروشنده‌ای که حداقل یک امتیاز داشته باشد
     */
    @Query("SELECT sr.advertisement.seller.id AS sellerId, " +
           "AVG(sr.rating) AS averageRating, " +
           "COUNT(sr) AS ratingCount " +
           "FROM SellerRating sr " +
           "WHERE sr.advertisement.seller.id IN :sellerIds " +
           "GROUP BY sr.advertisement.seller.id")
    List<SellerRatingAggregate> findRatingAggregatesBySellerIds(@Param("sellerIds") List<Long> sellerIds);

    /**
     * <h3>SellerRatingAggregate</h3>
     * <p>
     * پروجکشن (Projection Interface) نتیجه‌ی کوئری {@link #findRatingAggregatesBySellerIds}،
     * حاوی شناسه‌ی فروشنده به‌همراه میانگین و تعداد امتیازهای او.
     * </p>
     */
    interface SellerRatingAggregate {
        /**
         * @return شناسه فروشنده
         */
        Long getSellerId();
        /**
         * @return میانگین امتیازهای فروشنده
         */
        Double getAverageRating();
        /**
         * @return تعداد امتیازهای ثبت‌شده برای فروشنده
         */
        Long getRatingCount();
    }
}
