package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.Role;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainShellController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button adminPanelButton;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        String username = SessionManager.getInstance().getUsername();
        welcomeLabel.setText(username != null ? "خوش آمدید، " + username : "خوش آمدید!");

        Role role = SessionManager.getInstance().getRole();
        adminPanelButton.setVisible(role == Role.ADMIN);
        adminPanelButton.setManaged(role == Role.ADMIN);

        onConversationsClick();
    }

    private void loadIntoContentArea(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            AlertUtil.showError("بارگذاری این بخش با خطا مواجه شد.");
        }
    }

    @FXML
    private void onHomeClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    @FXML
    private void onMyAdvertisementsClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/my-advertisements.fxml", "آگهی‌های من");
    }

    @FXML
    private void onCreateAdvertisementClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/advertisement-form.fxml", "ثبت آگهی جدید");
    }

    @FXML
    private void onFavoritesClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/favorites.fxml", "علاقه‌مندی‌های من");
    }

    @FXML
    private void onConversationsClick() {
        loadIntoContentArea("/com/example/secondhandfx/fxml/conversation-list.fxml");
    }

    @FXML
    private void onAdminPanelClick() {
        loadIntoContentArea("/com/example/secondhandfx/fxml/admin-panel.fxml");
    }

    @FXML
    private void onLogoutButtonClick() {
        SessionManager.getInstance().clearSession();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
        AlertUtil.showSuccess("با موفقیت خارج شدید.");
    }
}