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
    @FXML private Label rejectionReasonLabel;

    private Runnable onView;
    private Runnable onEdit;
    private Runnable onMarkAsSold;
    private Runnable onDelete;

    public void setData(AdvertisementResponse ad) {
        titleLabel.setText(ad.getTitle());
        priceLabel.setText(formatPrice(ad.getPrice()) + " تومان");

        applyStatusBadge(ad.getStatus());
        applyActionVisibility(ad.getStatus());
        applyRejectionReason(ad.getStatus(), ad.getRejectionReason());
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
            case "APPROVED" -> { text = "فعال";            color = "#28a745"; }
            case "PENDING"  -> { text = "در انتظار بررسی"; color = "#ffc107"; }
            case "REJECTED" -> { text = "رد شده";          color = "#dc3545"; }
            case "SOLD"     -> { text = "فروخته‌شده";      color = "#6c757d"; }
            case "DELETED"  -> { text = "حذف‌شده";         color = "#6c757d"; }
            default         -> { text = status;             color = "#6c757d"; }
        }
        statusBadgeLabel.setText(text);
        statusBadgeLabel.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 4;");
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

    // فقط وقتی آگهی رد شده و دلیلی هم ثبت شده، این لیبل رو نشون می‌ده
    private void applyRejectionReason(String status, String rejectionReason) {
        boolean shouldShow = "REJECTED".equals(status)
                && rejectionReason != null && !rejectionReason.isBlank();

        rejectionReasonLabel.setVisible(shouldShow);
        rejectionReasonLabel.setManaged(shouldShow);

        if (shouldShow) {
            rejectionReasonLabel.setText("دلیل رد: " + rejectionReason);
        }
    }
}