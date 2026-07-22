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
        try {
            titleLabel.setText(ad.getTitle() != null ? ad.getTitle() : "بدون عنوان");
            priceLabel.setText((ad.getPrice() != null ? formatPrice(ad.getPrice()) : "۰") + " تومان");

            applyStatusBadge(ad.getStatus());
            applyActionVisibility(ad.getStatus());
            applyRejectionReason(ad.getStatus(), ad.getRejectionReason());

            System.out.println("✅ کارت برای آگهی ID: " + ad.getId() + " ساخته شد.");
        } catch (Exception e) {
            System.err.println("❌ خطا در setData برای آگهی ID: " + ad.getId());
            e.printStackTrace();
        }
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
        if (status == null) {
            text = "نامشخص";
            color = "-color-text-muted";
        } else {
            switch (status) {
                case "APPROVED":
                    text = "فعال";
                    color = "-color-success";
                    break;
                case "PENDING":
                    text = "در انتظار بررسی";
                    color = "-color-warning";
                    break;
                case "REJECTED":
                    text = "رد شده";
                    color = "-color-danger";
                    break;
                case "SOLD":
                    text = "فروخته‌شده";
                    color = "-color-text-muted";
                    break;
                case "DELETED":
                    text = "حذف‌شده";
                    color = "-color-text-muted";
                    break;
                default:
                    text = status;
                    color = "-color-text-muted";
                    break;
            }
        }
        statusBadgeLabel.setText(text);
        statusBadgeLabel.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 4;");
    }

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
        if (price == null) return "۰";
        return NumberFormat.getNumberInstance(Locale.US).format(price);
    }

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