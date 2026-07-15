package com.example.secondhand.service;

import com.example.secondhand.dto.response.ConversationResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.Advertisement;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.model.Conversation;
import com.example.secondhand.model.User;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.ConversationRepository;
import com.example.secondhand.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AdvertisementRepository advertisementRepository;

    public ConversationResponse startOrGetConversation(Long adId, User currentUser) {
        Advertisement advertisement = advertisementRepository.findById(adId)
                .orElseThrow(() -> new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد"));

        if (advertisement.getStatus() != AdvertisementStatus.APPROVED) {
            throw new AdvertisementNotFoundException("آگهی مورد نظر یافت نشد");
        }

        if (advertisement.getSeller().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("نمی‌توانید با آگهی خودتان گفت‌وگو شروع کنید");
        }

        Optional<Conversation> existing = conversationRepository
                .findByAdvertisementIdAndBuyerId(adId, currentUser.getId());

        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        Conversation conversation = Conversation.builder()
                .advertisement(advertisement)
                .buyer(currentUser)
                .build();

        return mapToResponse(conversationRepository.save(conversation));
    }

    private ConversationResponse mapToResponse(Conversation conversation) {
        String lastMessage = conversation.getMessages().isEmpty()
                ? null
                : conversation.getMessages()
                .get(conversation.getMessages().size() - 1)
                .getContent();

        long unreadCount = messageRepository
                .countByConversationIdAndIsReadFalseAndSenderIdNot(
                        conversation.getId(),
                        conversation.getBuyer().getId()
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

}
