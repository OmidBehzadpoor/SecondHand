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

    // ==================== startOrGetConversation ====================

    @Test
    void startOrGetConversation_shouldCreateNewConversation_whenNoneExists() {
        User seller = User.builder().id(1L).username("seller").status(UserStatus.ACTIVE).build();
        User buyer = User.builder().id(2L).username("buyer").status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .title("Laptop")
                .status(AdvertisementStatus.APPROVED)
                .seller(seller)
                .build();

        Conversation savedConversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .messages(new ArrayList<>())
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(conversationRepository.findByAdvertisementIdAndBuyerId(10L, 2L))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConversation);
        when(messageRepository.countByConversationIdAndIsReadFalseAndSenderIdNot(any(), any()))
                .thenReturn(0L);

        ConversationResponse response = chatService.startOrGetConversation(10L, buyer);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getAdvertisementId());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void startOrGetConversation_shouldReturnExistingConversation_whenAlreadyExists() {
        User seller = User.builder().id(1L).username("seller").status(UserStatus.ACTIVE).build();
        User buyer = User.builder().id(2L).username("buyer").status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .title("Laptop")
                .status(AdvertisementStatus.APPROVED)
                .seller(seller)
                .build();

        Conversation existing = Conversation.builder()
                .id(5L)
                .advertisement(ad)
                .buyer(buyer)
                .messages(new ArrayList<>())
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(conversationRepository.findByAdvertisementIdAndBuyerId(10L, 2L))
                .thenReturn(Optional.of(existing));
        when(messageRepository.countByConversationIdAndIsReadFalseAndSenderIdNot(any(), any()))
                .thenReturn(0L);

        ConversationResponse response = chatService.startOrGetConversation(10L, buyer);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    void startOrGetConversation_shouldThrowAdvertisementNotFoundException_whenAdDoesNotExist() {
        User buyer = User.builder().id(2L).build();

        when(advertisementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AdvertisementNotFoundException.class,
                () -> chatService.startOrGetConversation(99L, buyer));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGetConversation_shouldThrowAdvertisementNotFoundException_whenAdIsDeleted() {
        User seller = User.builder().id(1L).build();
        User buyer = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.DELETED)
                .seller(seller)
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> chatService.startOrGetConversation(10L, buyer));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGetConversation_shouldThrowAdvertisementNotFoundException_whenAdIsPending() {
        User seller = User.builder().id(1L).build();
        User buyer = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.PENDING)
                .seller(seller)
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(AdvertisementNotFoundException.class,
                () -> chatService.startOrGetConversation(10L, buyer));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGetConversation_shouldThrowUnauthorizedActionException_whenBuyerIsOwner() {
        User seller = User.builder().id(1L).status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.APPROVED)
                .seller(seller)
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(UnauthorizedActionException.class,
                () -> chatService.startOrGetConversation(10L, seller));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGetConversation_shouldThrowUserBlockedException_whenBuyerIsBlocked() {
        User seller = User.builder().id(1L).status(UserStatus.ACTIVE).build();
        User blockedBuyer = User.builder().id(2L).status(UserStatus.BLOCKED).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.APPROVED)
                .seller(seller)
                .build();

        assertThrows(UserBlockedException.class,
                () -> chatService.startOrGetConversation(10L, blockedBuyer));

        verify(conversationRepository, never()).save(any());
    }

    @Test
    void startOrGetConversation_shouldThrowUserBlockedException_whenSellerIsBlocked() {
        User blockedSeller = User.builder().id(1L).status(UserStatus.BLOCKED).build();
        User buyer = User.builder().id(2L).status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.APPROVED)
                .seller(blockedSeller)
                .build();

        when(advertisementRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThrows(UserBlockedException.class,
                () -> chatService.startOrGetConversation(10L, buyer));

        verify(conversationRepository, never()).save(any());
    }

    // ==================== sendMessage ====================

    @Test
    void sendMessage_shouldSaveMessage_whenSenderIsBuyer() {
        User seller = User.builder().id(1L).username("seller").status(UserStatus.ACTIVE).build();
        User buyer = User.builder().id(2L).username("buyer").status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .seller(seller)
                .build();

        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .build();

        Message savedMessage = Message.builder()
                .id(1L)
                .conversation(conversation)
                .sender(buyer)
                .content("سلام")
                .build();

        SendMessageRequest request = new SendMessageRequest("سلام");

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        MessageResponse response = chatService.sendMessage(1L, buyer, request);

        assertNotNull(response);
        assertEquals("سلام", response.getContent());
        assertEquals(2L, response.getSenderId());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void sendMessage_shouldSaveMessage_whenSenderIsSeller() {
        User seller = User.builder().id(1L).username("seller").status(UserStatus.ACTIVE).build();
        User buyer = User.builder().id(2L).username("buyer").status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .seller(seller)
                .build();

        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .build();

        Message savedMessage = Message.builder()
                .id(1L)
                .conversation(conversation)
                .sender(seller)
                .content("بله موجوده")
                .build();

        SendMessageRequest request = new SendMessageRequest("بله موجوده");

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        MessageResponse response = chatService.sendMessage(1L, seller, request);

        assertNotNull(response);
        assertEquals("بله موجوده", response.getContent());
        assertEquals(1L, response.getSenderId());
    }

    @Test
    void sendMessage_shouldSucceed_whenAdvertisementIsSold() {
        // Old conversations should keep working after the advertisement is marked SOLD.
        User seller = User.builder().id(1L).username("seller").status(UserStatus.ACTIVE).build();
        User buyer = User.builder().id(2L).username("buyer").status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.SOLD)
                .seller(seller)
                .build();

        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .build();

        Message savedMessage = Message.builder()
                .id(1L)
                .conversation(conversation)
                .sender(buyer)
                .content("هنوز آگهی موجوده؟")
                .build();

        SendMessageRequest request = new SendMessageRequest("هنوز آگهی موجوده؟");

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        MessageResponse response = chatService.sendMessage(1L, buyer, request);

        assertNotNull(response);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void sendMessage_shouldThrowAdvertisementNotFoundException_whenAdvertisementIsDeleted() {
        // EXPECTED business rule: once an advertisement is DELETED, its conversations should
        // no longer accept new messages — consistent with how startOrGetConversation already
        // rejects DELETED advertisements. sendMessage currently has NO such check, so this
        // test is expected to fail until that validation is added to the service.
        User seller = User.builder().id(1L).username("seller").status(UserStatus.ACTIVE).build();
        User buyer = User.builder().id(2L).username("buyer").status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .status(AdvertisementStatus.DELETED)
                .seller(seller)
                .build();

        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .build();

        SendMessageRequest request = new SendMessageRequest("سلام");

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        assertThrows(AdvertisementNotFoundException.class,
                () -> chatService.sendMessage(1L, buyer, request));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_shouldThrowConversationNotFoundException_whenConversationDoesNotExist() {
        User buyer = User.builder().id(2L).build();
        SendMessageRequest request = new SendMessageRequest("سلام");

        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> chatService.sendMessage(99L, buyer, request));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_shouldThrowUnauthorizedActionException_whenSenderIsNotMember() {
        User seller = User.builder().id(1L).status(UserStatus.ACTIVE).build();
        User buyer = User.builder().id(2L).status(UserStatus.ACTIVE).build();
        User stranger = User.builder().id(3L).status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder().id(10L).seller(seller).build();
        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .build();

        SendMessageRequest request = new SendMessageRequest("سلام");

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        assertThrows(UnauthorizedActionException.class,
                () -> chatService.sendMessage(1L, stranger, request));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_shouldThrowUserBlockedException_whenSenderIsBlocked() {
        User seller = User.builder().id(1L).status(UserStatus.ACTIVE).build();
        User blockedBuyer = User.builder().id(2L).status(UserStatus.BLOCKED).build();

        Advertisement ad = Advertisement.builder().id(10L).seller(seller).build();
        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(blockedBuyer)
                .build();

        SendMessageRequest request = new SendMessageRequest("سلام");

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        assertThrows(UserBlockedException.class,
                () -> chatService.sendMessage(1L, blockedBuyer, request));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void sendMessage_shouldThrowUserBlockedException_whenOtherPartyIsBlocked() {
        User blockedSeller = User.builder().id(1L).status(UserStatus.BLOCKED).build();
        User buyer = User.builder().id(2L).status(UserStatus.ACTIVE).build();

        Advertisement ad = Advertisement.builder().id(10L).seller(blockedSeller).build();
        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .build();

        SendMessageRequest request = new SendMessageRequest("سلام");

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        assertThrows(UserBlockedException.class,
                () -> chatService.sendMessage(1L, buyer, request));

        verify(messageRepository, never()).save(any());
    }

    // ==================== getMessages ====================

    @Test
    void getMessages_shouldReturnMessages_whenUserIsMember() {
        User seller = User.builder().id(1L).username("seller").build();
        User buyer = User.builder().id(2L).username("buyer").build();

        Advertisement ad = Advertisement.builder().id(10L).seller(seller).build();
        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .build();

        Message message = Message.builder()
                .id(1L)
                .conversation(conversation)
                .sender(buyer)
                .content("سلام")
                .build();

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(message));

        List<MessageResponse> responses = chatService.getMessages(1L, buyer);

        assertEquals(1, responses.size());
        assertEquals("سلام", responses.get(0).getContent());
        verify(messageRepository, times(1)).markMessagesAsRead(1L, 2L);
    }

    @Test
    void getMessages_shouldThrowConversationNotFoundException_whenConversationDoesNotExist() {
        User buyer = User.builder().id(2L).build();

        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> chatService.getMessages(99L, buyer));

        verify(messageRepository, never()).findByConversationIdOrderByCreatedAtAsc(any());
    }

    @Test
    void getMessages_shouldThrowUnauthorizedActionException_whenUserIsNotMember() {
        User seller = User.builder().id(1L).build();
        User buyer = User.builder().id(2L).build();
        User stranger = User.builder().id(3L).build();

        Advertisement ad = Advertisement.builder().id(10L).seller(seller).build();
        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .build();

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));

        assertThrows(UnauthorizedActionException.class,
                () -> chatService.getMessages(1L, stranger));

        verify(messageRepository, never()).findByConversationIdOrderByCreatedAtAsc(any());
    }

    @Test
    void getMessages_shouldReturnEmptyList_whenNoMessagesExist() {
        User seller = User.builder().id(1L).build();
        User buyer = User.builder().id(2L).build();

        Advertisement ad = Advertisement.builder().id(10L).seller(seller).build();
        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .build();

        when(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of());

        List<MessageResponse> responses = chatService.getMessages(1L, buyer);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    // ==================== getMyConversations ====================

    @Test
    void getMyConversations_shouldReturnConversations_whenUserHasSome() {
        User seller = User.builder().id(1L).username("seller").build();
        User buyer = User.builder().id(2L).username("buyer").build();

        Advertisement ad = Advertisement.builder()
                .id(10L)
                .title("Laptop")
                .seller(seller)
                .build();

        Conversation conversation = Conversation.builder()
                .id(1L)
                .advertisement(ad)
                .buyer(buyer)
                .messages(new ArrayList<>())
                .build();

        when(conversationRepository
                .findByBuyerIdOrAdvertisementSellerIdOrderByUpdatedAtDesc(2L, 2L))
                .thenReturn(List.of(conversation));
        when(messageRepository.countByConversationIdAndIsReadFalseAndSenderIdNot(any(), any()))
                .thenReturn(0L);

        List<ConversationResponse> responses = chatService.getMyConversations(buyer);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("Laptop", responses.get(0).getAdvertisementTitle());
    }

    @Test
    void getMyConversations_shouldReturnEmptyList_whenUserHasNone() {
        User buyer = User.builder().id(2L).build();

        when(conversationRepository
                .findByBuyerIdOrAdvertisementSellerIdOrderByUpdatedAtDesc(2L, 2L))
                .thenReturn(List.of());

        List<ConversationResponse> responses = chatService.getMyConversations(buyer);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}