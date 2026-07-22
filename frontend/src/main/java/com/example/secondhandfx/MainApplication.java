package com.example.secondhandfx;

import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private static VBox toastContainer;
    private static BorderPane mainContainer;

    @Override
    public void start(Stage primaryStage) {
        // Root StackPane
        StackPane root = new StackPane();

        // Main container (BorderPane)
        mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: -color-bg;");

        // Toast container (VBox at top-right)
        toastContainer = new VBox(10);
        toastContainer.setAlignment(Pos.TOP_RIGHT);
        toastContainer.setStyle("-fx-padding: 10;");
        toastContainer.setMouseTransparent(true); // اجازه‌ی کلیک به المان‌های زیرین

        // Add to root
        root.getChildren().addAll(mainContainer, toastContainer);

        // Create scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // Set references in SceneNavigator
        SceneNavigator.setPrimaryStage(primaryStage);
        SceneNavigator.setMainContainer(mainContainer);
        SceneNavigator.setToastContainer(toastContainer);

        // Apply theme
        ThemeManager.applyTheme(scene);

        // Window settings
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());

        // Navigate to home page
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static VBox getToastContainer() {
        return toastContainer;
    }

    public static void main(String[] args) {
        launch();
    }
}