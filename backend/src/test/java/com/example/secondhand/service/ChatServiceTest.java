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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private ChatService chatService;

    private User seller() {
        return User.builder().id(1L).username("seller").status(UserStatus.ACTIVE).build();
    }

    private User buyer() {
        return User.builder().id(2L).username("buyer").status(UserStatus.ACTIVE).build();
    }

    private void stubUnreadAndLastMessageLookups() {
        lenient().when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(any()))
                .thenReturn(Optional.empty());
        lenient().when(messageRepository.countByConversationIdAndIsReadFalseAndSenderIdNot(any(), any()))
                .thenReturn(0L);
    }

    // ==================== startOrGetConversation ====================

    @Test
    void startOrGetConversation_shouldThrowUserBlockedException_whenCurrentUserIsBlocked() {
        User blockedBuyer = User.builder().id(2L).status(UserStatus.BLOCKED).build();

        assertThrows(UserBlockedException.class,
                () -> chatService.startOrGetConversation(10L, blockedBuyer));

        verify(advertisementRepository, never()).findById(any());
    }

    @Test
    void startOrGetConversation_shouldThrowAdvertisementNotFoundException_whenAdDoesNotExist() {
        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> chatService.startOrGetConversation(99L, buyer()));
    }

    @Test
    void startOrGetConversation_shouldThrowAdvertisementNotFoundException_whenAdIsPending() {
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.PENDING).seller(seller()).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> chatService.startOrGetConversation(10L, buyer()));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGetConversation_shouldThrowAdvertisementNotFoundException_whenAdIsRejected() {
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.REJECTED).seller(seller()).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> chatService.startOrGetConversation(10L, buyer()));
    }

    @Test
    void startOrGetConversation_shouldThrowAdvertisementNotFoundException_whenAdIsDeleted() {
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.DELETED).seller(seller()).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> chatService.startOrGetConversation(10L, buyer()));
    }

    @Test
    void startOrGetConversation_shouldThrowUnauthorizedActionException_whenBuyerIsOwner() {
        User seller = seller();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(seller).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> chatService.startOrGetConversation(10L, seller));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGetConversation_shouldThrowUserBlockedException_whenSellerIsBlocked() {
        User blockedSeller = User.builder().id(1L).status(UserStatus.BLOCKED).build();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(blockedSeller).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(UserBlockedException.class,
                () -> chatService.startOrGetConversation(10L, buyer()));
    }

    @Test
    void startOrGetConversation_shouldReturnExistingConversation_whenOneAlreadyExists() {
        User seller = seller();
        User buyer = buyer();
        Advertisement ad = Advertisement.builder().id(10L).title("Laptop").status(AdvertisementStatus.APPROVED).seller(seller).build();
        Conversation existing = Conversation.builder().id(5L).advertisement(ad).buyer(buyer).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(conversationRepository.findByAdvertisementIdAndBuyerId(10L, 2L)).thenReturn(Optional.of(existing));
        stubUnreadAndLastMessageLookups();

        ConversationResponse response = chatService.startOrGetConversation(10L, buyer);

        assertEquals(5L, response.getId());
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGetConversation_shouldCreateNewConversation_whenNoneExists() {
        User seller = seller();
        User buyer = buyer();
        Advertisement ad = Advertisement.builder().id(10L).title("Laptop").status(AdvertisementStatus.APPROVED).seller(seller).build();
        Conversation saved = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(conversationRepository.findByAdvertisementIdAndBuyerId(10L, 2L)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(saved);
        stubUnreadAndLastMessageLookups();

        ConversationResponse response = chatService.startOrGetConversation(10L, buyer);

        assertNotNull(response);
        verify(conversationRepository, times(1)).save(any());
    }

    @Test
    void startOrGetConversation_shouldCreateNewConversation_whenAdvertisementIsSold() {
        // A SOLD advertisement is explicitly allowed by the status check (APPROVED or SOLD);
        // a new buyer starting a fresh conversation on it must succeed.
        User seller = seller();
        User buyer = buyer();
        Advertisement ad = Advertisement.builder().id(10L).title("Laptop").status(AdvertisementStatus.SOLD).seller(seller).build();
        Conversation saved = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(conversationRepository.findByAdvertisementIdAndBuyerId(10L, 2L)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(saved);
        stubUnreadAndLastMessageLookups();

        ConversationResponse response = chatService.startOrGetConversation(10L, buyer);

        assertNotNull(response);
    }

    // ==================== sendMessage ====================

    @Test
    void sendMessage_shouldSaveMessage_whenSenderIsBuyer() {
        User seller = seller();
        User buyer = buyer();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(seller).build();
        Conversation conversation = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();
        Message saved = Message.builder().id(1L).conversation(conversation).sender(buyer).content("سلام").build();

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("سلام");

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(saved);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        MessageResponse response = chatService.sendMessage(1L, buyer, request);

        assertEquals("سلام", response.getContent());
        verify(messageRepository, times(1)).save(any());
    }

    @Test
    void sendMessage_shouldSaveMessage_whenSenderIsSeller() {
        User seller = seller();
        User buyer = buyer();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(seller).build();
        Conversation conversation = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();
        Message saved = Message.builder().id(1L).conversation(conversation).sender(seller).content("سلام خریدار").build();

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("سلام خریدار");

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(saved);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        MessageResponse response = chatService.sendMessage(1L, seller, request);

        assertEquals("سلام خریدار", response.getContent());
    }

    @Test
    void sendMessage_shouldThrowConversationNotFoundException_whenConversationDoesNotExist() {
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("سلام");

        assertThrows(ConversationNotFoundException.class,
                () -> chatService.sendMessage(99L, buyer(), request));
    }

    @Test
    void sendMessage_shouldThrowUnauthorizedActionException_whenSenderIsNotMember() {
        User seller = seller();
        User buyer = buyer();
        User stranger = User.builder().id(3L).status(UserStatus.ACTIVE).build();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(seller).build();
        Conversation conversation = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("سلام");

        assertThrows(UnauthorizedActionException.class,
                () -> chatService.sendMessage(1L, stranger, request));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_shouldThrowUserBlockedException_whenSenderIsBlocked() {
        User seller = seller();
        User blockedBuyer = User.builder().id(2L).status(UserStatus.BLOCKED).build();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(seller).build();
        Conversation conversation = Conversation.builder().id(1L).advertisement(ad).buyer(blockedBuyer).build();

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("سلام");

        assertThrows(UserBlockedException.class,
                () -> chatService.sendMessage(1L, blockedBuyer, request));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_shouldThrowUserBlockedException_whenOtherPartyIsBlocked() {
        User blockedSeller = User.builder().id(1L).status(UserStatus.BLOCKED).build();
        User buyer = buyer();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(blockedSeller).build();
        Conversation conversation = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("سلام");

        assertThrows(UserBlockedException.class,
                () -> chatService.sendMessage(1L, buyer, request));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_stillSucceeds_onAConversationWhoseAdvertisementIsNowDeleted() {
        // EXPECTED business rule (still not implemented): once an advertisement is DELETED,
        // its conversations should stop accepting new messages, consistent with how
        // startOrGetConversation already rejects DELETED advertisements outright.
        // sendMessage still has no advertisement-status guard at all — it only checks
        // conversation membership and blocked-status — so this documents a real,
        // still-open gap. This test is EXPECTED TO FAIL until that check is added.
        User seller = seller();
        User buyer = buyer();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.DELETED).seller(seller).build();
        Conversation conversation = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();
        Message saved = Message.builder().id(1L).conversation(conversation).sender(buyer).content("سلام").build();

        SendMessageRequest request = new SendMessageRequest();
        request.setContent("سلام");

        lenient().when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        lenient().when(messageRepository.save(any(Message.class))).thenReturn(saved);
        lenient().when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        assertThrows(AdvertisementNotFoundException.class,
                () -> chatService.sendMessage(1L, buyer, request));
    }

    // ==================== getMyConversations ====================

    @Test
    void getMyConversations_shouldReturnConversationsForUser() {
        User seller = seller();
        User buyer = buyer();
        Advertisement ad = Advertisement.builder().id(10L).title("Laptop").status(AdvertisementStatus.APPROVED).seller(seller).build();
        Conversation conversation = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();

        when(conversationRepository.findByBuyerIdOrAdvertisementSellerIdOrderByUpdatedAtDesc(2L, 2L))
                .thenReturn(List.of(conversation));
        stubUnreadAndLastMessageLookups();

        List<ConversationResponse> result = chatService.getMyConversations(buyer);

        assertEquals(1, result.size());
    }

    // ==================== getMessages ====================

    @Test
    void getMessages_shouldReturnMessages_whenUserIsMember() {
        User seller = seller();
        User buyer = buyer();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(seller).build();
        Conversation conversation = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();
        Message message = Message.builder().id(1L).conversation(conversation).sender(seller).content("سلام").build();

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(message));

        List<MessageResponse> result = chatService.getMessages(1L, buyer);

        assertEquals(1, result.size());
        verify(messageRepository, times(1)).markMessagesAsRead(1L, 2L);
    }

    @Test
    void getMessages_shouldThrowUnauthorizedActionException_whenUserIsNotMember() {
        User seller = seller();
        User buyer = buyer();
        User stranger = User.builder().id(3L).build();
        Advertisement ad = Advertisement.builder().id(10L).status(AdvertisementStatus.APPROVED).seller(seller).build();
        Conversation conversation = Conversation.builder().id(1L).advertisement(ad).buyer(buyer).build();

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        assertThrows(UnauthorizedActionException.class,
                () -> chatService.getMessages(1L, stranger));

        verify(messageRepository, never()).markMessagesAsRead(any(), any());
    }

    @Test
    void getMessages_shouldThrowConversationNotFoundException_whenConversationDoesNotExist() {
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> chatService.getMessages(99L, buyer()));
    }
}
