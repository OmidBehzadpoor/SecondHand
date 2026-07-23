package com.example.secondhand.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <h2>Conversation</h2>
 * <p>
 * موجودیت (Entity) نگاشت‌شده به جدول {@code conversations}، نماینده‌ی یک
 * <b>گفت‌وگو</b> بین یک خریدار ({@link #buyer}) و فروشنده‌ی یک آگهی مشخص
 * ({@link #advertisement}). برای هر ترکیب از آگهی و خریدار، حداکثر یک
 * گفت‌وگو می‌تواند وجود داشته باشد (محدودیت یکتایی روی
 * {@code advertisement_id} و {@code buyer_id}).
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.Message
 * @see com.example.secondhand.service.ChatService
 */
@Entity
@Table(
        name = "conversations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"advertisement_id", "buyer_id"})
        }
)@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    /** شناسه یکتای گفت‌وگو (کلید اصلی، تولید خودکار). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** آگهی‌ای که این گفت‌وگو درباره‌ی آن است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private Advertisement advertisement;

    /** کاربری که در نقش خریدار این گفت‌وگو را شروع کرده است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    /** زمان ایجاد گفت‌وگو؛ پس از ایجاد قابل تغییر نیست. */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** زمان آخرین به‌روزرسانی گفت‌وگو (مثلاً هنگام ارسال پیام جدید). */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** لیست پیام‌های رد‌وبدل‌شده در این گفت‌وگو. */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
}
