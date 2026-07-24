package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.MessageResponse;
import com.example.secondhandfx.util.SessionManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * <h2>MessageListCell</h2>
 * <p>
 * سلول سفارشی (Custom {@link ListCell}) برای نمایش یک <b>پیام</b> در فهرست
 * پیام‌های یک گفت‌وگو، به شکل حباب چت (Chat Bubble). پیام‌های ارسالی توسط
 * کاربر جاری و پیام‌های طرف مقابل با استایل و چیدمان متفاوت (راست/چپ)
 * نمایش داده می‌شوند تا جهت مکالمه از نظر بصری واضح باشد.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.controller.ChatViewController
 */
public class MessageListCell extends ListCell<MessageResponse> {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * به‌روزرسانی محتوای بصری سلول بر اساس آیتم پیام داده‌شده.
     * <p>
     * در صورت خالی بودن سلول یا {@code null} بودن آیتم، محتوای سلول پاک
     * می‌شود. در غیر این صورت، بر اساس اینکه فرستنده‌ی پیام کاربر جاری باشد
     * یا خیر، یک حباب چت با استایل و چیدمان (راست‌چین برای پیام‌های خودم،
     * چپ‌چین برای پیام‌های طرف مقابل) به‌همراه متن پیام و زمان ارسال ساخته
     * می‌شود.
     * </p>
     *
     * @param message آیتم پیام مرتبط با این سلول
     * @param empty   نشان‌دهنده‌ی خالی بودن سلول (بدون آیتم معتبر)
     */
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
