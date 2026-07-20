package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ConversationResponse;
import com.example.secondhandfx.model.MessageResponse;
import com.example.secondhandfx.model.SendMessageRequest;

import java.util.List;

public interface ChatService {

    ConversationResponse startOrGetConversation(Long advertisementId) throws ApiException;

    List<ConversationResponse> getMyConversations() throws ApiException;

    List<MessageResponse> getMessages(Long conversationId) throws ApiException;

    MessageResponse sendMessage(Long conversationId, SendMessageRequest request) throws ApiException;
}