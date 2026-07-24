package com.example.secondhand.repository;

import com.example.secondhand.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * <h2>MessageRepository</h2>
 * <p>
 * ریپازیتوری Spring Data JPA برای موجودیت {@link Message}. علاوه بر عملیات
 * پایه‌ی CRUD فراهم‌شده توسط {@link JpaRepository}، شامل متدهای مشتق‌شده
 * برای شمارش پیام‌های نخوانده و دریافت پیام‌های یک گفت‌وگو، و یک کوئری
 * سفارشی به‌روزرسانی‌کننده ({@link #markMessagesAsRead}) است.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.ChatService
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * شمارش تعداد پیام‌های نخوانده‌ی یک گفت‌وگو برای یک کاربر مشخص (یعنی
     * پیام‌هایی که فرستنده‌ی آن‌ها کاربر داده‌شده نیست و هنوز خوانده نشده‌اند).
     *
     * @param conversationId شناسه گفت‌وگو
     * @param senderId       شناسه کاربری که پیام‌های ارسالی توسط او باید نادیده گرفته شوند
     * @return تعداد پیام‌های نخوانده برای این کاربر در این گفت‌وگو
     */
    long countByConversationIdAndIsReadFalseAndSenderIdNot(Long conversationId, Long senderId);

    /**
     * دریافت تمام پیام‌های یک گفت‌وگو، مرتب‌شده بر اساس زمان ایجاد (صعودی).
     *
     * @param conversationId شناسه گفت‌وگو
     * @return لیست پیام‌های گفت‌وگو به ترتیب زمانی ارسال
     */
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /**
     * دریافت آخرین پیام ارسال‌شده در یک گفت‌وگو (بر اساس زمان ایجاد نزولی).
     *
     * @param conversationId شناسه گفت‌وگو
     * @return آخرین پیام در قالب {@link Optional}، یا خالی در صورت نبود هیچ پیامی
     */
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);


    /**
     * علامت‌گذاری تمام پیام‌های نخوانده‌ی یک گفت‌وگو (به‌جز پیام‌های ارسالی
     * توسط خود کاربر) به‌عنوان خوانده‌شده.
     * <p>
     * این متد یک عملیات به‌روزرسانی مستقیم (Bulk Update) است؛ با
     * {@code clearAutomatically = true} کش سطح اول Hibernate پس از اجرا
     * پاک‌سازی می‌شود تا داده‌های قدیمی در حافظه باقی نمانند.
     * </p>
     *
     * @param conversationId شناسه گفت‌وگویی که پیام‌های آن باید خوانده‌شده علامت زده شوند
     * @param userId         شناسه کاربری که پیام‌های ارسالی توسط او نباید تغییر کنند
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.isRead = false")
    void markMessagesAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
