package com.example.secondhandfx.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertUtil {

    private AlertUtil() {
    }

    public static void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.setHeaderText("خطا");
            alert.showAndWait();
        });
    }

    public static void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.setHeaderText("موفقیت‌آمیز");
            alert.showAndWait();
        });
    }
}