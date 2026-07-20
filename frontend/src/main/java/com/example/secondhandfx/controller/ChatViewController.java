package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ConversationResponse;
import com.example.secondhandfx.model.MessageResponse;
import com.example.secondhandfx.model.SendMessageRequest;
import com.example.secondhandfx.service.ChatService;
import com.example.secondhandfx.service.ChatServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;

public class ChatViewController {

    @FXML
    private Label headerLabel;

    @FXML
    private ListView<MessageResponse> messageListView;

    @FXML
    private TextField messageInputField;

    private final ChatService chatService = new ChatServiceImpl();
    private final ObservableList<MessageResponse> messages = FXCollections.observableArrayList();

    private Long conversationId;

    @FXML
    public void initialize() {
        messageListView.setCellFactory(listView -> new MessageListCell());
        messageListView.setItems(messages);

        messageInputField.setOnAction(event -> onSendButtonClick());
    }

    public void setConversation(ConversationResponse conversation) {
        this.conversationId = conversation.getId();

        Long currentUserId = SessionManager.getInstance().getUserId();
        boolean isBuyer = conversation.getBuyerId().equals(currentUserId);
        String otherPartyUsername = isBuyer
                ? conversation.getSellerUsername()
                : conversation.getBuyerUsername();

        headerLabel.setText(conversation.getAdvertisementTitle() + " — " + otherPartyUsername);

        loadMessages();
    }

    private void loadMessages() {
        Task<List<MessageResponse>> loadTask = new Task<>() {
            @Override
            protected List<MessageResponse> call() throws Exception {
                return chatService.getMessages(conversationId);
            }
        };

        loadTask.setOnSucceeded(event -> {
            messages.setAll(loadTask.getValue());
        });

        loadTask.setOnFailed(event -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();
            String message = (ex instanceof ApiException)
                    ? ex.getMessage()
                    : "خطای ناشناخته‌ای در دریافت پیام‌ها رخ داد.";
            AlertUtil.showError(message);
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void onSendButtonClick() {
        String content = messageInputField.getText().trim();

        if (content.isEmpty()) {
            return;
        }

        SendMessageRequest request = SendMessageRequest.builder()
                .content(content)
                .build();

        Task<MessageResponse> sendTask = new Task<>() {
            @Override
            protected MessageResponse call() throws Exception {
                return chatService.sendMessage(conversationId, request);
            }
        };

        sendTask.setOnSucceeded(event -> {
            messages.add(sendTask.getValue());
            messageInputField.clear();
        });

        sendTask.setOnFailed(event -> {
            Throwable ex = sendTask.getException();
            ex.printStackTrace();
            String message = (ex instanceof ApiException)
                    ? ex.getMessage()
                    : "خطای ناشناخته‌ای در ارسال پیام رخ داد.";
            AlertUtil.showError(message);
        });

        new Thread(sendTask).start();
    }
}