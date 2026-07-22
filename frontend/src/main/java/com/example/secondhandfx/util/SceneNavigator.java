package com.example.secondhandfx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {

    private static Stage primaryStage;
    private static BorderPane mainContainer;
    private static VBox toastContainer;

    private SceneNavigator() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void setMainContainer(BorderPane container) {
        mainContainer = container;
    }

    public static void setToastContainer(VBox container) {
        toastContainer = container;
    }

    public static VBox getToastContainer() {
        return toastContainer;
    }

    public static FXMLLoader navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Set content in main container
            mainContainer.setCenter(root);

            // Apply theme to the scene (if exists)
            if (primaryStage != null) {
                Scene scene = primaryStage.getScene();
                if (scene != null) {
                    ThemeManager.applyTheme(scene);
                }
                primaryStage.setTitle(title);
            }

            return loader;
        } catch (IOException e) {
            throw new IllegalStateException("بارگذاری صفحه‌ی " + fxmlPath + " با خطا مواجه شد", e);
        }
    }
}