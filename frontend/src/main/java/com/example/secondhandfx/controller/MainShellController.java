package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.Role;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class MainShellController {

    private static final double SIDEBAR_WIDTH = 220;
    private static final Duration SLIDE_DURATION = Duration.millis(220);

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button adminPanelButton;

    @FXML
    private StackPane contentArea;

    @FXML
    private StackPane sidebarOverlay;

    @FXML
    private VBox sidebarBox;

    private boolean sidebarOpen = false;

    @FXML
    public void initialize() {
        String username = SessionManager.getInstance().getUsername();
        welcomeLabel.setText(username != null ? "خوش آمدید، " + username : "خوش آمدید!");

        Role role = SessionManager.getInstance().getRole();
        adminPanelButton.setVisible(role == Role.ADMIN);
        adminPanelButton.setManaged(role == Role.ADMIN);

        // ساید‌بار به‌صورت پیش‌فرض بسته است و با دکمه‌ی «☰ منو» به‌صورت کشویی باز می‌شود
        sidebarBox.setTranslateX(-SIDEBAR_WIDTH);

        // بارگذاری پیش‌فرض: گفتگوها
        onConversationsClick();
    }

    @FXML
    private void onToggleSidebarClick() {
        sidebarOpen = !sidebarOpen;

        TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, sidebarBox);
        slide.setToX(sidebarOpen ? 0 : -SIDEBAR_WIDTH);
        slide.play();
    }

    private void closeSidebar() {
        if (sidebarOpen) {
            sidebarOpen = false;
            TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, sidebarBox);
            slide.setToX(-SIDEBAR_WIDTH);
            slide.play();
        }
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
        closeSidebar();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/my-advertisements.fxml", "آگهی‌های من");
    }

    @FXML
    private void onCreateAdvertisementClick() {
        closeSidebar();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/advertisement-form.fxml", "ثبت آگهی جدید");
    }

    @FXML
    private void onFavoritesClick() {
        closeSidebar();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/favorites.fxml", "علاقه‌مندی‌های من");
    }

    @FXML
    private void onConversationsClick() {
        loadIntoContentArea("/com/example/secondhandfx/fxml/conversation-list.fxml");
        closeSidebar();
    }

    @FXML
    private void onAdminPanelClick() {
        loadIntoContentArea("/com/example/secondhandfx/fxml/admin-panel.fxml");
        closeSidebar();
    }

    @FXML
    private void onLogoutButtonClick() {
        SessionManager.getInstance().clearSession();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
        AlertUtil.showSuccess("با موفقیت خارج شدید.");
    }
}