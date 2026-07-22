package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.LoginRequest;
import com.example.secondhandfx.model.LoginResponse;
import com.example.secondhandfx.service.AuthService;
import com.example.secondhandfx.service.AuthServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import com.example.secondhandfx.util.ValidationUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthServiceImpl();

    @FXML
    private void onLoginButtonClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(password)) {
            AlertUtil.showError("لطفاً نام کاربری و رمز عبور را وارد کنید.");
            return;
        }

        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        Task<LoginResponse> loginTask = new Task<>() {
            @Override
            protected LoginResponse call() throws Exception {
                return authService.login(loginRequest);
            }
        };

        loginTask.setOnSucceeded(event -> {
            LoginResponse response = loginTask.getValue();
            SessionManager.getInstance().setSession(
                    response.getToken(),
                    response.getUserId(),
                    response.getUsername(),
                    response.getRole()
            );
            AlertUtil.showSuccess(response.getName() + "، به سامانه البرز خوش آمدید!");
            SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");        });

        loginTask.setOnFailed(event -> {
            Throwable ex = loginTask.getException();
            String errorMessage = ex.getMessage();
            if (ex instanceof ApiException) {
                // پیام خطا از سرور
            } else {
                errorMessage = "خطای ناشناخته‌ای رخ داد. لطفاً دوباره تلاش کنید.";
            }
            AlertUtil.showError(errorMessage);
        });

        new Thread(loginTask).start();
    }

    @FXML
    private void onBackToHomeClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    @FXML
    private void onRegisterLinkClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/register.fxml", "ثبت‌نام");
    }
}