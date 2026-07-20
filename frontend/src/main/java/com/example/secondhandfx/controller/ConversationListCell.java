package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.ConversationResponse;
import com.example.secondhandfx.util.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ConversationListCell extends ListCell<ConversationResponse> {

    @Override
    protected void updateItem(ConversationResponse conversation, boolean empty) {
        super.updateItem(conversation, empty);

        if (empty || conversation == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        Long currentUserId = SessionManager.getInstance().getUserId();
        boolean isBuyer = conversation.getBuyerId().equals(currentUserId);
        String otherPartyUsername = isBuyer
                ? conversation.getSellerUsername()
                : conversation.getBuyerUsername();

        Label titleLabel = new Label(conversation.getAdvertisementTitle() + " — " + otherPartyUsername);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        String preview = conversation.getLastMessage() != null
                ? conversation.getLastMessage()
                : "هنوز پیامی ارسال نشده";
        Label previewLabel = new Label(preview);
        previewLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        VBox textBox = new VBox(4, titleLabel, previewLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox row = new HBox(10, textBox);
        row.setPadding(new Insets(8));

        if (conversation.getUnreadCount() > 0) {
            Label unreadBadge = new Label(String.valueOf(conversation.getUnreadCount()));
            unreadBadge.setStyle(
                    "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                            "-fx-background-radius: 12; -fx-padding: 2 8; -fx-font-size: 11px;"
            );
            row.getChildren().add(unreadBadge);
        }

        setText(null);
        setGraphic(row);
    }
}