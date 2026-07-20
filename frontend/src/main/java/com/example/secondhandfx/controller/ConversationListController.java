package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ConversationResponse;
import com.example.secondhandfx.service.ChatService;
import com.example.secondhandfx.service.ChatServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ConversationListController implements Initializable {

    @FXML
    private ListView<ConversationResponse> conversationListView;

    private final ChatService chatService = new ChatServiceImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conversationListView.setCellFactory(listView -> new ConversationListCell());

        conversationListView.setOnMouseClicked(event -> {
            ConversationResponse selected = conversationListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                onConversationSelected(selected);
            }
        });

        loadConversations();
    }

    private void loadConversations() {
        Task<List<ConversationResponse>> loadTask = new Task<>() {
            @Override
            protected List<ConversationResponse> call() throws Exception {
                return chatService.getMyConversations();
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<ConversationResponse> conversations = loadTask.getValue();
            conversationListView.setItems(FXCollections.observableArrayList(conversations));
        });

        loadTask.setOnFailed(event -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();
            String message = (ex instanceof ApiException)
                    ? ex.getMessage()
                    : "خطای ناشناخته‌ای در دریافت گفتگوها رخ داد.";
            AlertUtil.showError(message);
        });

        new Thread(loadTask).start();
    }

    private void onConversationSelected(ConversationResponse conversation) {
        AlertUtil.showSuccess("صفحه‌ی چت به زودی اضافه می‌شود! (گفتگوی انتخاب‌شده: " + conversation.getAdvertisementTitle() + ")");
    }
}