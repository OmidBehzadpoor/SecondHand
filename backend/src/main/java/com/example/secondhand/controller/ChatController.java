package com.example.secondhand.controller;

import com.example.secondhand.dto.SendMessageRequest;
import com.example.secondhand.dto.response.ApiResponse;
import com.example.secondhand.dto.response.ConversationResponse;
import com.example.secondhand.dto.response.MessageResponse;
import com.example.secondhand.model.User;
import com.example.secondhand.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;


    @PostMapping("/conversations/advertisement/{adId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> startOrGetConversation(
            @PathVariable Long adId,
            @AuthenticationPrincipal User currentUser) {
        ConversationResponse response = chatService.startOrGetConversation(adId, currentUser);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "CONVERSATION_READY", response));
    }


    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations(
            @AuthenticationPrincipal User currentUser) {
        List<ConversationResponse> responses = chatService.getMyConversations(currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "CONVERSATIONS_RETRIEVED", responses));
    }


    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal User currentUser) {
        List<MessageResponse> responses = chatService.getMessages(conversationId, currentUser);
        return ResponseEntity.ok(new ApiResponse<>(true, "MESSAGES_RETRIEVED", responses));
    }


    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User currentUser) {
        MessageResponse response = chatService.sendMessage(conversationId, currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "MESSAGE_SENT", response));
    }
}