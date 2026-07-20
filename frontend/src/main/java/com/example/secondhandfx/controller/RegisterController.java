package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.RegisterRequest;
import com.example.secondhandfx.service.AuthService;
import com.example.secondhandfx.service.AuthServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.concurrent.Task;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    private final AuthService authService = new AuthServiceImpl();

    @FXML
    private void onRegisterButtonClick() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()
                || phone.isEmpty() || email.isEmpty()) {
            AlertUtil.showError("لطفاً همه‌ی فیلدها را پر کنید.");
            return;
        }

        if (password.length() < 4) {
            AlertUtil.showError("رمز عبور باید حداقل ۴ کاراکتر باشد.");
            return;
        }

        RegisterRequest registerRequest = RegisterRequest.builder()
                .name(name)
                .username(username)
                .password(password)
                .phone(phone)
                .email(email)
                .build();

        Task<Long> registerTask = new Task<>() {
            @Override
            protected Long call() throws Exception {
                return authService.register(registerRequest);
            }
        };

        registerTask.setOnSucceeded(event -> {
            AlertUtil.showSuccess("ثبت‌نام با موفقیت انجام شد. اکنون می‌توانید وارد شوید.");
            SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
        });

        registerTask.setOnFailed(event -> {
            Throwable ex = registerTask.getException();
            String errorMessage = ex.getMessage();
            if (!(ex instanceof ApiException)) {
                errorMessage = "خطای ناشناخته‌ای رخ داد. لطفاً دوباره تلاش کنید.";
            }
            AlertUtil.showError(errorMessage);
        });

        new Thread(registerTask).start();
    }

    @FXML
    private void onLoginLinkClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
    }
}
