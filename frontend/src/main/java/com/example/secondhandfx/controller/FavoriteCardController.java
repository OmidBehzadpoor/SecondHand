package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.FavoriteResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.Locale;

public class FavoriteCardController {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label cityLabel;
    @FXML private Label statusBadgeLabel;
    @FXML private Button viewButton;
    @FXML private Button removeButton;

    private Runnable onView;
    private Runnable onRemove;

    public void setData(FavoriteResponse favorite) {
        titleLabel.setText(favorite.getAdvertisementTitle());
        priceLabel.setText(formatPrice(favorite.getPrice()) + " تومان");
        cityLabel.setText(favorite.getCityName());
        applyStatusBadge(favorite.getAdvertisementStatus());
    }

    public void setOnView(Runnable handler) {
        this.onView = handler;
    }

    public void setOnRemove(Runnable handler) {
        this.onRemove = handler;
    }

    private void applyStatusBadge(String status) {
        String text;
        String styleClass;
        switch (status) {
            case "APPROVED" -> { text = "فعال"; styleClass = "status-approved"; }
            case "PENDING" -> { text = "در انتظار بررسی"; styleClass = "status-pending"; }
            case "REJECTED" -> { text = "رد شده"; styleClass = "status-rejected"; }
            case "SOLD" -> { text = "فروخته‌شده"; styleClass = "status-sold"; }
            case "DELETED" -> { text = "حذف‌شده توسط مدیر"; styleClass = "status-deleted"; }
            default -> { text = status; styleClass = "status-deleted"; }
        }
        statusBadgeLabel.setText(text);
        statusBadgeLabel.getStyleClass().removeIf(c -> c.startsWith("status-") && !c.equals("status-badge"));
        statusBadgeLabel.getStyleClass().add(styleClass);
    }

    @FXML
    private void onViewClick() {
        if (onView != null) onView.run();
    }

    @FXML
    private void onRemoveClick() {
        if (onRemove != null) onRemove.run();
    }

    private String formatPrice(Long price) {
        if (price == null) return "-";
        return NumberFormat.getNumberInstance(Locale.US).format(price);
    }
}