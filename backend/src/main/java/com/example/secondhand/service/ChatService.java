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

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AdvertisementRepository advertisementRepository;

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

    private ConversationResponse mapToResponse(Conversation conversation, User currentUser) {
        String lastMessage = messageRepository
                .findLastMessageByConversationId(conversation.getId())
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