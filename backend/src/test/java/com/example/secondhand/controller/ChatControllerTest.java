package com.example.secondhand.controller;

import com.example.secondhand.dto.SendMessageRequest;
import com.example.secondhand.dto.response.ConversationResponse;
import com.example.secondhand.dto.response.MessageResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.exception.UserBlockedException;
import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final User CURRENT_USER = User.builder().id(2L).username("buyer").role(Role.USER).build();

    @BeforeEach
    void setUp() {
        ChatController controller = new ChatController(chatService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        CURRENT_USER, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private SendMessageRequest messageRequest(String content) {
        SendMessageRequest request = new SendMessageRequest();
        request.setContent(content);
        return request;
    }

    @Test
    void startOrGetConversation_shouldReturn200_whenSuccessful() throws Exception {
        ConversationResponse response = ConversationResponse.builder().id(1L).advertisementId(10L).build();

        when(chatService.startOrGetConversation(eq(10L), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(post("/api/chat/conversations/advertisement/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void startOrGetConversation_shouldReturn404_whenAdvertisementDoesNotExist() throws Exception {
        when(chatService.startOrGetConversation(eq(99L), eq(CURRENT_USER)))
                .thenThrow(new AdvertisementNotFoundException("آگهی یافت نشد"));

        mockMvc.perform(post("/api/chat/conversations/advertisement/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void startOrGetConversation_shouldReturn403_whenBuyerIsOwner() throws Exception {
        when(chatService.startOrGetConversation(eq(10L), eq(CURRENT_USER)))
                .thenThrow(new UnauthorizedActionException("شما نمی‌توانید به آگهی خودتان پیام دهید"));

        mockMvc.perform(post("/api/chat/conversations/advertisement/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyConversations_shouldReturn200_withList() throws Exception {
        ConversationResponse conv = ConversationResponse.builder().id(1L).build();

        when(chatService.getMyConversations(eq(CURRENT_USER))).thenReturn(List.of(conv));

        mockMvc.perform(get("/api/chat/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getMessages_shouldReturn200_whenUserIsMember() throws Exception {
        MessageResponse msg = MessageResponse.builder().id(1L).content("سلام").build();

        when(chatService.getMessages(eq(1L), eq(CURRENT_USER))).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/chat/conversations/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("سلام"));
    }

    @Test
    void getMessages_shouldReturn403_whenUserIsNotMember() throws Exception {
        when(chatService.getMessages(eq(1L), eq(CURRENT_USER)))
                .thenThrow(new UnauthorizedActionException("شما عضو این گفتگو نیستید"));

        mockMvc.perform(get("/api/chat/conversations/1/messages"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sendMessage_shouldReturn201_whenDataIsValid() throws Exception {
        MessageResponse response = MessageResponse.builder().id(1L).content("سلام، هنوز موجوده؟").build();

        when(chatService.sendMessage(eq(1L), eq(CURRENT_USER), any(SendMessageRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/chat/conversations/1/messages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(messageRequest("سلام، هنوز موجوده؟"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("سلام، هنوز موجوده؟"));
    }

    @Test
    void sendMessage_shouldReturn400_whenContentIsBlank() throws Exception {
        mockMvc.perform(post("/api/chat/conversations/1/messages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(messageRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendMessage_shouldReturn403_whenSenderIsBlocked() throws Exception {
        when(chatService.sendMessage(eq(1L), eq(CURRENT_USER), any(SendMessageRequest.class)))
                .thenThrow(new UserBlockedException("حساب کاربری شما مسدود است"));

        mockMvc.perform(post("/api/chat/conversations/1/messages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(messageRequest("سلام"))))
                .andExpect(status().isForbidden());
    }
}
