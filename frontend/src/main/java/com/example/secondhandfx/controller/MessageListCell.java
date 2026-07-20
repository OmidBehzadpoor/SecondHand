package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.MessageResponse;
import com.example.secondhandfx.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

public class MessageListCell extends ListCell<MessageResponse> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    protected void updateItem(MessageResponse message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        Long currentUserId = SessionManager.getInstance().getUserId();
        boolean isMine = message.getSenderId().equals(currentUserId);

        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(320);

        Label timeLabel = new Label(message.getCreatedAt().format(TIME_FORMATTER));
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: derive(-fx-text-fill, 40%);");

        javafx.scene.layout.VBox bubble = new javafx.scene.layout.VBox(4, contentLabel, timeLabel);
        bubble.setPadding(new Insets(8, 12, 8, 12));

        if (isMine) {
            bubble.setStyle("-fx-background-color: #3498db; -fx-background-radius: 12;");
            contentLabel.setStyle("-fx-text-fill: white;");
            timeLabel.setStyle(timeLabel.getStyle() + " -fx-text-fill: #eaf3fb;");
        } else {
            bubble.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 12;");
            contentLabel.setStyle("-fx-text-fill: #2c3e50;");
        }

        HBox row = new HBox(bubble);
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 10, 4, 10));

        setText(null);
        setGraphic(row);
        setStyle("-fx-background-color: transparent;");
    }
}