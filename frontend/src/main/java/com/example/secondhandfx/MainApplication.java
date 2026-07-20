package com.example.secondhandfx;

import com.example.secondhandfx.util.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneNavigator.setPrimaryStage(primaryStage);
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}