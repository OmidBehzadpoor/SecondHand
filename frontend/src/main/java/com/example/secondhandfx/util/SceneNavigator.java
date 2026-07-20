package com.example.secondhandfx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {

    private static Stage primaryStage;

    private SceneNavigator() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static FXMLLoader navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            ThemeManager.applyTheme(scene);

            // قبل از عوض‌کردن صحنه، سایز فعلی پنجره رو نگه می‌داریم
            // تا صفحه‌ی جدید (که ممکنه prefWidth/prefHeight کوچیک‌تری داشته باشه)
            // پنجره رو کوچیک نکنه
            boolean wasMaximized = primaryStage.isMaximized();
            double previousWidth = primaryStage.getWidth();
            double previousHeight = primaryStage.getHeight();

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else if (previousWidth > 0 && previousHeight > 0) {
                primaryStage.setWidth(previousWidth);
                primaryStage.setHeight(previousHeight);
            }

            return loader;
        } catch (IOException e) {
            throw new IllegalStateException("بارگذاری صفحه‌ی " + fxmlPath + " با خطا مواجه شد", e);
        }
    }
}