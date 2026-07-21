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

            // سایز واقعیِ صحنه‌ی قبلی (نه سایز پنجره که حاشیه/تایتل‌بار هم داره) رو نگه می‌داریم
            boolean wasMaximized = primaryStage.isMaximized();
            Scene previousScene = primaryStage.getScene();
            double previousWidth = previousScene != null ? previousScene.getWidth() : 0;
            double previousHeight = previousScene != null ? previousScene.getHeight() : 0;

            // سایز رو مستقیم موقع ساخت Scene جدید می‌دیم، نه بعد از set کردنش روی Stage؛
            // این روش قابل‌اتکاتره و با رفتار داخلی ری‌سایز خودکار جاوافایکس تداخل نداره
            Scene scene = (previousWidth > 0 && previousHeight > 0)
                    ? new Scene(root, previousWidth, previousHeight)
                    : new Scene(root);
            ThemeManager.applyTheme(scene);

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

            if (wasMaximized) {
                primaryStage.setMaximized(true);
            }

            return loader;
        } catch (IOException e) {
            throw new IllegalStateException("بارگذاری صفحه‌ی " + fxmlPath + " با خطا مواجه شد", e);
        }
    }
}