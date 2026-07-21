package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.MessageResponse;
import javafx.scene.control.ListCell;

public class MessageListCell extends ListCell<MessageResponse> {

    @Override
    protected void updateItem(MessageResponse message, boolean empty) {
        super.updateItem(message, empty);
        if (empty || message == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(message.getContent()); // فقط متن ساده
        }
    }
}