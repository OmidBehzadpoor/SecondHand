package com.example.secondhandfx.util;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AlertUtil {

    private static final int TOAST_DURATION_SECONDS = 3;

    private AlertUtil() {
    }

    public static void showError(String message) {
        showToast(message, "toast-error");
    }

    public static void showSuccess(String message) {
        showToast(message, "toast-success");
    }

    public static void showInfo(String message) {
        showToast(message, "toast-info");
    }

    private static void showToast(String message, String typeClass) {
        Platform.runLater(() -> {
            VBox toastContainer = SceneNavigator.getToastContainer();
            if (toastContainer == null) {
                showAlertFallback(message);
                return;
            }

            HBox toast = createToast(message, typeClass);
            toastContainer.getChildren().add(toast);

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(TOAST_DURATION_SECONDS), toast);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                toastContainer.getChildren().remove(toast);
            });
            fadeOut.play();
        });
    }

    private static HBox createToast(String message, String typeClass) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().addAll("toast-label");

        HBox toast = new HBox(label);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().addAll("toast", typeClass);

        toast.setMaxWidth(400);
        toast.setMouseTransparent(true);
        return toast;
    }

    private static void showAlertFallback(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                message,
                javafx.scene.control.ButtonType.OK
        );
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}