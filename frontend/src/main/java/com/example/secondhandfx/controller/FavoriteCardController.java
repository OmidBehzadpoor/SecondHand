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
        String color;
        switch (status) {
            case "APPROVED" -> { text = "فعال"; color = "#27ae60"; }
            case "SOLD" -> { text = "فروخته‌شده"; color = "#7f8c8d"; }
            case "DELETED" -> { text = "حذف‌شده توسط مدیر"; color = "#95a5a6"; }
            default -> { text = status; color = "#7f8c8d"; }
        }
        statusBadgeLabel.setText(text);
        statusBadgeLabel.setStyle("-fx-text-fill: white; -fx-background-color: " + color
                + "; -fx-padding: 2 10; -fx-background-radius: 10; -fx-font-size: 11px;");
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