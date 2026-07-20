package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.AdvertisementResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.Locale;

public class MyAdvertisementCardController {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label statusBadgeLabel;
    @FXML private Button viewButton;
    @FXML private Button editButton;
    @FXML private Button markAsSoldButton;
    @FXML private Button deleteButton;

    private Runnable onView;
    private Runnable onEdit;
    private Runnable onMarkAsSold;
    private Runnable onDelete;

    public void setData(AdvertisementResponse ad) {
        titleLabel.setText(ad.getTitle());
        priceLabel.setText(formatPrice(ad.getPrice()) + " تومان");

        applyStatusBadge(ad.getStatus());
        applyActionVisibility(ad.getStatus());
    }

    public void setOnView(Runnable handler) {
        this.onView = handler;
    }

    public void setOnEdit(Runnable handler) {
        this.onEdit = handler;
    }

    public void setOnMarkAsSold(Runnable handler) {
        this.onMarkAsSold = handler;
    }

    public void setOnDelete(Runnable handler) {
        this.onDelete = handler;
    }

    private void applyStatusBadge(String status) {
        String text;
        String color;
        switch (status) {
            case "APPROVED" -> { text = "فعال"; color = "#27ae60"; }
            case "PENDING" -> { text = "در انتظار بررسی"; color = "#f39c12"; }
            case "REJECTED" -> { text = "رد شده"; color = "#e74c3c"; }
            case "SOLD" -> { text = "فروخته‌شده"; color = "#7f8c8d"; }
            case "DELETED" -> { text = "حذف‌شده"; color = "#95a5a6"; }
            default -> { text = status; color = "#7f8c8d"; }
        }
        statusBadgeLabel.setText(text);
        statusBadgeLabel.setStyle("-fx-text-fill: white; -fx-background-color: " + color
                + "; -fx-padding: 2 10; -fx-background-radius: 10; -fx-font-size: 11px;");
    }

    // بر اساس محدودیت‌های واقعی بک‌اند تصمیم می‌گیره کدام دکمه‌ها نشون داده بشن:
    // markAsSold فقط برای APPROVED مجازه، ویرایش برای SOLD/DELETED منطقی نیست،
    // حذف برای هر چیزی به‌جز DELETED مجازه.
    private void applyActionVisibility(String status) {
        boolean isDeleted = "DELETED".equals(status);
        boolean isSold = "SOLD".equals(status);
        boolean isApproved = "APPROVED".equals(status);

        editButton.setVisible(!isDeleted && !isSold);
        editButton.setManaged(!isDeleted && !isSold);

        markAsSoldButton.setVisible(isApproved);
        markAsSoldButton.setManaged(isApproved);

        deleteButton.setVisible(!isDeleted);
        deleteButton.setManaged(!isDeleted);
    }

    @FXML
    private void onViewClick() {
        if (onView != null) onView.run();
    }

    @FXML
    private void onEditClick() {
        if (onEdit != null) onEdit.run();
    }

    @FXML
    private void onMarkAsSoldClick() {
        if (onMarkAsSold != null) onMarkAsSold.run();
    }

    @FXML
    private void onDeleteClick() {
        if (onDelete != null) onDelete.run();
    }

    private String formatPrice(Long price) {
        if (price == null) return "-";
        return NumberFormat.getNumberInstance(Locale.US).format(price);
    }
}