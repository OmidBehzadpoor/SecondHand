package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.ConversationResponse;
import com.example.secondhandfx.model.MessageResponse;
import com.example.secondhandfx.model.SellerRatingRequest;
import com.example.secondhandfx.model.SellerRatingResponse;
import com.example.secondhandfx.model.SendMessageRequest;
import com.example.secondhandfx.service.ChatService;
import com.example.secondhandfx.service.ChatServiceImpl;
import com.example.secondhandfx.service.RatingService;
import com.example.secondhandfx.service.RatingServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChatViewController {

    @FXML
    private Label headerLabel;

    @FXML
    private Button rateSellerButton;

    @FXML
    private ListView<MessageResponse> messageListView;

    @FXML
    private TextField messageInputField;

    private final ChatService chatService = new ChatServiceImpl();
    private final RatingService ratingService = new RatingServiceImpl();
    private final ObservableList<MessageResponse> messages = FXCollections.observableArrayList();

    private Long conversationId;
    private Long advertisementId;
    private boolean isBuyer;

    @FXML
    public void initialize() {
        messageListView.setCellFactory(listView -> new MessageListCell());
        messageListView.setItems(messages);

        messageInputField.setOnAction(event -> onSendButtonClick());
    }

    public void setConversation(ConversationResponse conversation) {
        this.conversationId = conversation.getId();
        this.advertisementId = conversation.getAdvertisementId();

        Long currentUserId = SessionManager.getInstance().getUserId();
        this.isBuyer = conversation.getBuyerId().equals(currentUserId);

        String otherPartyUsername = isBuyer
                ? conversation.getSellerUsername()
                : conversation.getBuyerUsername();

        headerLabel.setText(conversation.getAdvertisementTitle() + " — " + otherPartyUsername);

        rateSellerButton.setVisible(isBuyer);
        rateSellerButton.setManaged(isBuyer);

        loadMessages();
    }

    private void loadMessages() {
        Task<List<MessageResponse>> loadTask = new Task<>() {
            @Override
            protected List<MessageResponse> call() throws Exception {
                return chatService.getMessages(conversationId);
            }
        };

        loadTask.setOnSucceeded(event -> messages.setAll(loadTask.getValue()));

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

    @FXML
    private void onBackClick() {
        // بازگشت به لیست گفتگوها (با حفظ سایدبار/ساختار اصلی)
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/conversation-list.fxml", "گفتگوها");
    }

    @FXML
    private void onRateSellerClick() {
        Dialog<SellerRatingRequest> dialog = new Dialog<>();
        dialog.setHeaderText("امتیازدهی به فروشنده");

        ChoiceBox<Integer> ratingChoice = new ChoiceBox<>();
        ratingChoice.getItems().addAll(1, 2, 3, 4, 5);
        ratingChoice.setValue(5);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("نظر شما (اختیاری)");
        commentArea.setPrefRowCount(3);

        VBox content = new VBox(10,
                new Label("امتیاز (از ۱ تا ۵):"), ratingChoice,
                new Label("نظر:"), commentArea);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return SellerRatingRequest.builder()
                        .rating(ratingChoice.getValue())
                        .comment(commentArea.getText())
                        .build();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(request -> {
            Task<SellerRatingResponse> task = new Task<>() {
                @Override
                protected SellerRatingResponse call() throws Exception {
                    return ratingService.rateAdvertisement(advertisementId, request);
                }
            };

            task.setOnSucceeded(e -> AlertUtil.showSuccess("امتیاز شما ثبت شد."));

            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                ex.printStackTrace();
                String message = (ex instanceof ApiException)
                        ? ex.getMessage()
                        : "خطا در ثبت امتیاز رخ داد.";
                AlertUtil.showError(message);
            });

            new Thread(task).start();
        });
    }
}