package com.example.secondhand.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * <h2>Message</h2>
 * <p>
 * موجودیت (Entity) نگاشت‌شده به جدول {@code messages}، نماینده‌ی یک
 * <b>پیام</b> ارسال‌شده در یک {@link Conversation} توسط یکی از دو طرف
 * گفت‌وگو (خریدار یا فروشنده).
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.ChatService
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /** شناسه یکتای پیام (کلید اصلی، تولید خودکار). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** گفت‌وگویی که این پیام به آن تعلق دارد. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /** کاربری که این پیام را ارسال کرده است. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** متن محتوای پیام. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** زمان ارسال پیام؛ پس از ایجاد قابل تغییر نیست. */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** وضعیت خوانده‌شدن پیام توسط طرف مقابل؛ به‌طور پیش‌فرض {@code false} است. */
    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;
}
