package com.example.secondhandfx;

import com.example.secondhandfx.util.SceneNavigator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneNavigator.setPrimaryStage(primaryStage);

        Label welcomeLabel = new Label("به بهترین سامانه خرید و فروش دست دوم دنیا که توسط امید و پارسا طراحی شده است خوش آمدید");

        StackPane root = new StackPane(welcomeLabel);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("SecondHand Market");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
