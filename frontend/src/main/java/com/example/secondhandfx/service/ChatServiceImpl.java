package com.example.secondhandfx.service;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ApiResponse;
import com.example.secondhandfx.model.ConversationResponse;
import com.example.secondhandfx.model.MessageResponse;
import com.example.secondhandfx.model.SendMessageRequest;
import com.example.secondhandfx.util.HttpClientHelper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class ChatServiceImpl implements ChatService {

    @Override
    public ConversationResponse startOrGetConversation(Long advertisementId) throws ApiException {
        return HttpClientHelper.post(
                "/api/chat/conversations/advertisement/" + advertisementId,
                null,
                new TypeReference<ApiResponse<ConversationResponse>>() {}
        ).getData();
    }

    @Override
    public List<ConversationResponse> getMyConversations() throws ApiException {
        return HttpClientHelper.get(
                "/api/chat/conversations",
                new TypeReference<ApiResponse<List<ConversationResponse>>>() {}
        ).getData();
    }

    @Override
    public List<MessageResponse> getMessages(Long conversationId) throws ApiException {
        return HttpClientHelper.get(
                "/api/chat/conversations/" + conversationId + "/messages",
                new TypeReference<ApiResponse<List<MessageResponse>>>() {}
        ).getData();
    }

    @Override
    public MessageResponse sendMessage(Long conversationId, SendMessageRequest request) throws ApiException {
        return HttpClientHelper.post(
                "/api/chat/conversations/" + conversationId + "/messages",
                request,
                new TypeReference<ApiResponse<MessageResponse>>() {}
        ).getData();
    }
}