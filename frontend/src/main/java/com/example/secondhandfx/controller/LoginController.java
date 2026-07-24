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

/**
 * <h2>LoginController</h2>
 * <p>
 * کنترلر صفحه‌ی <b>ورود (Login)</b> کاربر. اعتبارسنجی ساده‌ی سمت کلاینت را
 * روی فیلدهای فرم انجام می‌دهد، سپس درخواست ورود را در یک {@link Task} پس‌زمینه
 * به بک‌اند ارسال می‌کند تا رابط کاربری در حین انتظار برای پاسخ سرور مسدود نشود.
 * در صورت موفقیت، نشست کاربر از طریق {@link SessionManager} ثبت و کاربر به
 * صفحه‌ی اصلی هدایت می‌شود.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.service.AuthService
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthServiceImpl();

    /**
     * پردازش کلیک روی دکمه‌ی ورود.
     * <p>
     * ابتدا خالی نبودن نام کاربری و رمز عبور بررسی می‌شود؛ سپس درخواست ورود
     * به‌صورت غیرهمزمان (در یک {@link Thread} جداگانه) ارسال می‌شود. در صورت
     * موفقیت، نشست کاربر ثبت و پیام خوش‌آمدگویی نمایش داده می‌شود و کاربر به
     * صفحه‌ی خانه هدایت می‌شود؛ در صورت شکست، پیام خطای مناسب نمایش داده می‌شود.
     * </p>
     */
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
                    response.getRole(),
                    response.getName()
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

    /**
     * پردازش کلیک روی دکمه‌ی بازگشت به صفحه‌ی خانه.
     */
    @FXML
    private void onBackToHomeClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    /**
     * پردازش کلیک روی لینک ثبت‌نام، و هدایت کاربر به صفحه‌ی ثبت‌نام.
     */
    @FXML
    private void onRegisterLinkClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/register.fxml", "ثبت‌نام");
    }
}
