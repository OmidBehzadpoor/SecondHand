package com.example.secondhandfx.controller;

import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String username = SessionManager.getInstance().getUsername();
        if (username != null) {
            welcomeLabel.setText("خوش آمدید، " + username + " 👋");
        } else {
            welcomeLabel.setText("خوش آمدید!");
        }
    }

    @FXML
    private void onLogoutButtonClick() {
        SessionManager.getInstance().clearSession();

        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");

        AlertUtil.showSuccess("با موفقیت خارج شدید.");
    }

    @FXML
    private void onCreateAdvertisementClick() {
        AlertUtil.showSuccess("صفحه‌ی ثبت آگهی به زودی اضافه می‌شود!");
    }

    @FXML
    private void onMyAdvertisementsClick() {
        AlertUtil.showSuccess("صفحه‌ی آگهی‌های من به زودی اضافه می‌شود!");
    }
}