package com.example.secondhandfx.util;

import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

public class AlertUtil {

    private AlertUtil() {
    }

    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "خطا", message, "error-dialog");
    }

    public static void showSuccess(String message) {
        showAlert(Alert.AlertType.INFORMATION, "موفقیت‌آمیز", message, "success-dialog");
    }

    private static void showAlert(Alert.AlertType type, String header, String message, String typeStyleClass) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type, message, ButtonType.OK);
            alert.setHeaderText(header);
            alert.setTitle(header);
            alert.setGraphic(null);

            DialogPane pane = alert.getDialogPane();
            pane.getStylesheets().addAll(
                    AlertUtil.class.getResource(
                            "/css/theme-" + ThemeManager.getCurrentTheme().name().toLowerCase() + ".css"
                    ).toExternalForm(),
                    AlertUtil.class.getResource("/css/components.css").toExternalForm()
            );
            pane.getStyleClass().addAll("app-dialog", typeStyleClass);
            pane.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            pane.setPrefWidth(380);

            alert.showAndWait();
        });
    }
}