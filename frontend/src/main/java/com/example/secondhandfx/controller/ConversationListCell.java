package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.ConversationResponse;
import com.example.secondhandfx.util.SessionManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * <h2>ConversationListCell</h2>
 * <p>
 * سلول سفارشی (Custom {@link ListCell}) برای نمایش یک <b>گفت‌وگو</b> در
 * فهرست گفت‌وگوهای کاربر. برای هر ردیف، عنوان آگهی به‌همراه نام کاربری طرف
 * مقابل گفت‌وگو (خریدار یا فروشنده، بسته به نقش کاربر جاری)، پیش‌نمایش آخرین
 * پیام، و در صورت وجود پیام نخوانده، یک نشان (Badge) تعداد پیام‌های نخوانده
 * نمایش داده می‌شود.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.controller.ConversationListController
 */
public class ConversationListCell extends ListCell<ConversationResponse> {

    /**
     * به‌روزرسانی محتوای بصری سلول بر اساس آیتم گفت‌وگوی داده‌شده.
     * <p>
     * در صورت خالی بودن سلول یا {@code null} بودن آیتم، محتوای سلول پاک
     * می‌شود. در غیر این صورت، بر اساس اینکه کاربر جاری خریدار یا فروشنده‌ی
     * گفت‌وگو باشد، نام کاربری طرف مقابل تعیین شده و یک ردیف شامل عنوان
     * آگهی، پیش‌نمایش آخرین پیام و (در صورت وجود) نشان تعداد پیام‌های
     * نخوانده ساخته و نمایش داده می‌شود.
     * </p>
     *
     * @param conversation آیتم گفت‌وگوی مرتبط با این سلول
     * @param empty        نشان‌دهنده‌ی خالی بودن سلول (بدون آیتم معتبر)
     */
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
        titleLabel.getStyleClass().add("list-cell-title");

        String preview = conversation.getLastMessage() != null
                ? conversation.getLastMessage()
                : "هنوز پیامی ارسال نشده";
        Label previewLabel = new Label(preview);
        previewLabel.getStyleClass().add("list-cell-subtitle");

        VBox textBox = new VBox(4, titleLabel, previewLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        HBox row = new HBox(10, textBox);
        row.setPadding(new Insets(8));

        if (conversation.getUnreadCount() > 0) {
            Label unreadBadge = new Label(String.valueOf(conversation.getUnreadCount()));
            unreadBadge.getStyleClass().addAll("badge", "badge-danger");
            row.getChildren().add(unreadBadge);
        }

        setText(null);
        setGraphic(row);
    }
}
