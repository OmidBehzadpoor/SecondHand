package com.example.secondhand.service;

import com.example.secondhand.dto.SendMessageRequest;
import com.example.secondhand.dto.response.ConversationResponse;
import com.example.secondhand.dto.response.MessageResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.ConversationNotFoundException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.exception.UserBlockedException;
import com.example.secondhand.model.*;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.ConversationRepository;
import com.example.secondhand.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * <h2>ChatService</h2>
 * <p>
 * سرویس مسئول مدیریت <b>گفت‌وگوها (Conversations)</b> و <b>پیام‌ها (Messages)</b>
 * بین خریدار و فروشنده، در ارتباط با یک آگهی مشخص.
 * </p>
 * <ul>
 *   <li>شروع یا بازیابی یک گفت‌وگوی موجود بین خریدار و فروشنده‌ی یک آگهی</li>
 *   <li>ارسال پیام در یک گفت‌وگو، با بررسی عضویت فرستنده و مسدود نبودن طرفین</li>
 *   <li>دریافت لیست گفت‌وگوهای کاربر جاری، به‌همراه آخرین پیام و تعداد پیام‌های نخوانده</li>
 *   <li>دریافت پیام‌های یک گفت‌وگو و علامت‌گذاری آن‌ها به‌عنوان خوانده‌شده</li>
 * </ul>
 * <p>
 * در تمام عملیات، وضعیت مسدود بودن ({@code UserStatus.BLOCKED}) کاربر جاری یا
 * طرف مقابل گفت‌وگو بررسی می‌شود تا از ارسال یا شروع گفت‌وگو توسط/با کاربران
 * مسدودشده جلوگیری شود.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.Conversation
 * @see com.example.secondhand.model.Message
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AdvertisementRepository advertisementRepository;

    /**
     * شروع یک گفت‌وگوی جدید با فروشنده‌ی یک آگهی، یا بازگرداندن گفت‌وگوی
     * موجود در صورتی که کاربر جاری قبلاً برای همین آگهی گفت‌وگویی شروع کرده باشد.
     * <p>
     * آگهی باید در وضعیت {@code APPROVED} یا {@code SOLD} باشد، کاربر جاری
     * نباید فروشنده‌ی همان آگهی باشد، و هیچ‌کدام از کاربر جاری یا فروشنده
     * نباید مسدود باشند.
     * </p>
     *
     * @param adId        شناسه آگهی‌ای که گفت‌وگو درباره‌ی آن شروع می‌شود
     * @param currentUser کاربری که قصد شروع گفت‌وگو (به‌عنوان خریدار) را دارد
     * @return {@link ConversationResponse} حاوی اطلاعات گفت‌وگوی جدید یا موجود
     * @throws UserBlockedException در صورتی که کاربر جاری یا فروشنده مسدود باشند
     * @throws AdvertisementNotFoundException در صورتی که آگهی یافت نشود یا در
     *         وضعیتی نباشد که امکان گفت‌وگو درباره‌ی آن وجود داشته باشد
     * @throws UnauthorizedActionException در صورتی که کاربر جاری بخواهد با
     *         آگهی خودش گفت‌وگو شروع کند
     */
    @Transactional
    public ConversationResponse startOrGetConversation(Long adId, User currentUser) {

        if (currentUser.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException("حساب کاربری شما مسدود شده است");
        }

        Advertisement advertisement = advertisementRepository.findById(adId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (advertisement.getStatus() != AdvertisementStatus.APPROVED
                && advertisement.getStatus() != AdvertisementStatus.SOLD) {
            throw new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد");
        }

        if (advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("نمی‌توانید با آگهی خودتان گفت‌وگو شروع کنید");
        }

        if (advertisement.getSeller().getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException("امکان شروع گفت‌وگو با این فروشنده وجود ندارد");
        }

        Optional<Conversation> existing = conversationRepository
                .findByAdvertisementIdAndBuyerId(adId, currentUser.getId());

        if (existing.isPresent()) {
            return mapToResponse(existing.get(), currentUser);
        }

        Conversation conversation = Conversation.builder()
                .advertisement(advertisement)
                .buyer(currentUser)
                .build();

        return mapToResponse(conversationRepository.save(conversation), currentUser);
    }

    /**
     * ارسال یک پیام جدید در یک گفت‌وگوی موجود.
     * <p>
     * فرستنده باید یکی از دو طرف گفت‌وگو (خریدار یا فروشنده) باشد و نه
     * فرستنده و نه طرف مقابل نباید مسدود باشند.
     * </p>
     *
     * @param conversationId شناسه گفت‌وگویی که پیام باید در آن ارسال شود
     * @param currentUser    کاربری که پیام را ارسال می‌کند
     * @param request        محتوای پیام ارسالی
     * @return {@link MessageResponse} حاوی اطلاعات پیام ذخیره‌شده
     * @throws ConversationNotFoundException در صورتی که گفت‌وگو یافت نشود
     * @throws UnauthorizedActionException در صورتی که کاربر جاری عضو این گفت‌وگو نباشد
     * @throws UserBlockedException در صورتی که کاربر جاری یا طرف مقابل گفت‌وگو مسدود باشند
     */
    @Transactional
    public MessageResponse sendMessage(Long conversationId, User currentUser, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("گفت‌وگو مورد نظر یافت نشد"));

        boolean isBuyer = conversation.getBuyer().getId().equals(currentUser.getId());
        boolean isSeller = conversation.getAdvertisement().getSeller().getId().equals(currentUser.getId());

        if (!isBuyer && !isSeller) {
            throw new UnauthorizedActionException("شما عضو این گفت‌وگو نیستید");
        }

        if (currentUser.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException("حساب کاربری شما مسدود شده است");
        }

        User otherParty = isBuyer
                ? conversation.getAdvertisement().getSeller()
                : conversation.getBuyer();

        if (otherParty.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException("امکان ارسال پیام به این کاربر وجود ندارد");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(currentUser)
                .content(request.getContent())
                .build();

        Message savedMessage = messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return mapToResponse(savedMessage);
    }

    /**
     * دریافت لیست تمام گفت‌وگوهای کاربر جاری (چه به‌عنوان خریدار و چه به‌عنوان
     * فروشنده)، مرتب‌شده بر اساس آخرین به‌روزرسانی (نزولی).
     *
     * @param currentUser کاربری که گفت‌وگوهای او باید بازگردانده شود
     * @return لیستی از {@link ConversationResponse} مرتبط با کاربر جاری
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations(User currentUser) {
        return conversationRepository
                .findByBuyerIdOrAdvertisementSellerIdOrderByUpdatedAtDesc(
                        currentUser.getId(),
                        currentUser.getId()
                )
                .stream()
                .map(conversation -> mapToResponse(conversation, currentUser))
                .toList();
    }

    /**
     * دریافت تمام پیام‌های یک گفت‌وگو و علامت‌گذاری پیام‌های دریافتی به‌عنوان
     * خوانده‌شده برای کاربر جاری.
     *
     * @param conversationId شناسه گفت‌وگویی که پیام‌های آن باید دریافت شود
     * @param currentUser    کاربری که درخواست مشاهده‌ی پیام‌ها را داده است
     * @return لیستی از {@link MessageResponse} مرتب‌شده بر اساس زمان ایجاد (صعودی)
     * @throws ConversationNotFoundException در صورتی که گفت‌وگو یافت نشود
     * @throws UnauthorizedActionException در صورتی که کاربر جاری عضو این گفت‌وگو نباشد
     */
    @Transactional
    public List<MessageResponse> getMessages(Long conversationId, User currentUser) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("گفت‌وگو مورد نظر یافت نشد"));

        boolean isMember = conversation.getBuyer().getId().equals(currentUser.getId())
                || conversation.getAdvertisement().getSeller().getId().equals(currentUser.getId());

        if (!isMember) {
            throw new UnauthorizedActionException("شما عضو این گفت‌وگو نیستید");
        }

        messageRepository.markMessagesAsRead(conversationId, currentUser.getId());

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * تبدیل شیء {@link Conversation} به DTO خروجی {@link ConversationResponse}،
     * به‌همراه آخرین پیام گفت‌وگو و تعداد پیام‌های نخوانده برای کاربر جاری.
     *
     * @param conversation موجودیت گفت‌وگو
     * @param currentUser  کاربر جاری، برای محاسبه‌ی تعداد پیام‌های نخوانده‌ی او
     * @return شیء {@link ConversationResponse} متناظر با گفت‌وگو
     */
    private ConversationResponse mapToResponse(Conversation conversation, User currentUser) {
        String lastMessage = messageRepository
                .findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .map(Message::getContent)
                .orElse(null);

        long unreadCount = messageRepository
                .countByConversationIdAndIsReadFalseAndSenderIdNot(
                        conversation.getId(),
                        currentUser.getId()
                );

        return ConversationResponse.builder()
                .id(conversation.getId())
                .advertisementId(conversation.getAdvertisement().getId())
                .advertisementTitle(conversation.getAdvertisement().getTitle())
                .buyerId(conversation.getBuyer().getId())
                .buyerUsername(conversation.getBuyer().getUsername())
                .sellerId(conversation.getAdvertisement().getSeller().getId())
                .sellerUsername(conversation.getAdvertisement().getSeller().getUsername())
                .lastMessage(lastMessage)
                .unreadCount((int) unreadCount)
                .updatedAt(conversation.getUpdatedAt())
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    /**
     * تبدیل شیء {@link Message} به DTO خروجی {@link MessageResponse}.
     *
     * @param message موجودیت پیام
     * @return شیء {@link MessageResponse} متناظر با پیام
     */
    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isRead(message.isRead())
                .build();
    }
}
