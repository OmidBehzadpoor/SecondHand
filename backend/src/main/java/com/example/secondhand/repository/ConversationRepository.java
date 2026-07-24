package com.example.secondhand.repository;

import com.example.secondhand.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <h2>ConversationRepository</h2>
 * <p>
 * ریپازیتوری Spring Data JPA برای موجودیت {@link Conversation}. علاوه بر
 * عملیات پایه‌ی CRUD فراهم‌شده توسط {@link JpaRepository}، شامل متدهای
 * مشتق‌شده (Derived Query) برای یافتن گفت‌وگوی موجود بین خریدار و آگهی، و
 * دریافت لیست گفت‌وگوهای یک کاربر (چه در نقش خریدار و چه فروشنده) است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.ChatService
 */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * یافتن گفت‌وگوی موجود بین یک آگهی و یک خریدار مشخص.
     *
     * @param advertisementId شناسه آگهی
     * @param buyerId         شناسه خریدار
     * @return گفت‌وگوی موجود در قالب {@link Optional}، یا خالی در صورت عدم وجود
     */
    Optional<Conversation> findByAdvertisementIdAndBuyerId(Long advertisementId, Long buyerId);

    /**
     * دریافت لیست تمام گفت‌وگوهایی که کاربر جاری در آن‌ها خریدار یا فروشنده
     * (فروشنده‌ی آگهی مرتبط) است، مرتب‌شده بر اساس آخرین به‌روزرسانی (نزولی).
     *
     * @param buyerId  شناسه‌ی کاربر برای بررسی به‌عنوان خریدار
     * @param sellerId شناسه‌ی کاربر برای بررسی به‌عنوان فروشنده‌ی آگهی گفت‌وگو
     * @return لیست گفت‌وگوهای مرتبط با کاربر، مرتب‌شده بر اساس جدیدترین به‌روزرسانی
     */
    List<Conversation> findByBuyerIdOrAdvertisementSellerIdOrderByUpdatedAtDesc(Long buyerId, Long sellerId);
}
