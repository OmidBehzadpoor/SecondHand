package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.MessageResponse;
import com.example.secondhandfx.util.SessionManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class MessageListCell extends ListCell<MessageResponse> {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    protected void updateItem(MessageResponse message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setText(null);
            setGraphic(null);
            setStyle(null);
            return;
        }

        boolean isMine = message.getSenderId().equals(SessionManager.getInstance().getUserId());

        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add(isMine ? "chat-text-mine" : "chat-text-theirs");

        Label timeLabel = new Label(message.getCreatedAt() != null ? message.getCreatedAt().format(TIME_FORMAT) : "");
        timeLabel.getStyleClass().add("chat-timestamp");

        timeLabel.setMaxWidth(Double.MAX_VALUE);
        timeLabel.setAlignment(Pos.CENTER_RIGHT);

        VBox bubble = new VBox(4, contentLabel, timeLabel);
        bubble.getStyleClass().addAll("chat-bubble", isMine ? "chat-bubble-mine" : "chat-bubble-theirs");
        bubble.setMaxWidth(320);

        HBox row = new HBox(bubble);
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);

        setText(null);
        setGraphic(row);
        setStyle("-fx-background-color: transparent; -fx-padding: 2 0;");
    }
}