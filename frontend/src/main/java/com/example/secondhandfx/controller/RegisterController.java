package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.LoginRequest;
import com.example.secondhandfx.model.LoginResponse;
import com.example.secondhandfx.model.RegisterRequest;
import com.example.secondhandfx.service.AuthService;
import com.example.secondhandfx.service.AuthServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import com.example.secondhandfx.util.ValidationUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * <h2>RegisterController</h2>
 * <p>
 * کنترلر صفحه‌ی <b>ثبت‌نام</b> کاربر جدید. اعتبارسنجی کامل سمت کلاینت (خالی
 * نبودن فیلدها، طول رمز عبور، فرمت ایمیل و شماره تماس) را پیش از ارسال
 * درخواست انجام می‌دهد. پس از ثبت‌نام موفق، به‌صورت خودکار با همان نام
 * کاربری و رمز عبور، ورود ({@link #autoLogin}) نیز انجام می‌شود تا کاربر
 * مجبور به وارد کردن مجدد اطلاعات نباشد.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.service.AuthService
 */
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

    /**
     * پردازش کلیک روی دکمه‌ی ثبت‌نام.
     * <p>
     * ابتدا تمام قوانین اعتبارسنجی سمت کلاینت (خالی نبودن فیلدها، حداقل طول
     * رمز عبور، فرمت معتبر ایمیل و شماره تماس) بررسی می‌شوند. در صورت عبور
     * از اعتبارسنجی، درخواست ثبت‌نام به‌صورت غیرهمزمان ارسال می‌شود و در
     * صورت موفقیت، مرحله‌ی ورود خودکار از طریق {@link #autoLogin(String, String)}
     * آغاز می‌شود.
     * </p>
     */
    @FXML
    private void onRegisterButtonClick() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (ValidationUtil.isBlank(name) || ValidationUtil.isBlank(username)
                || ValidationUtil.isBlank(password) || ValidationUtil.isBlank(phone)
                || ValidationUtil.isBlank(email)) {
            AlertUtil.showError("لطفاً همه‌ی فیلدها را پر کنید.");
            return;
        }

        if (password.length() < 4) {
            AlertUtil.showError("رمز عبور باید حداقل ۴ کاراکتر باشد.");
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            AlertUtil.showError("ایمیل واردشده معتبر نیست.");
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            AlertUtil.showError("شماره تماس باید ۱۱ رقم و با ۰۹ شروع شود.");
            return;
        }

        RegisterRequest registerRequest = RegisterRequest.builder()
                .name(name)
                .username(username)
                .password(password)
                .phone(phone)
                .email(email)
                .build();

        // مرحله‌ی ۱: ثبت‌نام
        Task<Long> registerTask = new Task<>() {
            @Override
            protected Long call() throws Exception {
                return authService.register(registerRequest);
            }
        };

        registerTask.setOnSucceeded(event -> {
            Long userId = registerTask.getValue();
            System.out.println("✅ ثبت‌نام موفق برای کاربر: " + username + " (ID: " + userId + ")");

            // مرحله‌ی ۲: لاگین خودکار با همان username و password
            autoLogin(username, password);
        });

        registerTask.setOnFailed(event -> {
            Throwable ex = registerTask.getException();
            String errorMessage = (ex instanceof ApiException) ? ex.getMessage() : "خطای ناشناخته‌ای رخ داد.";
            AlertUtil.showError(errorMessage);
        });

        new Thread(registerTask).start();
    }

    // متد کمکی برای لاگین خودکار
    /**
     * انجام ورود خودکار پس از ثبت‌نام موفق، با استفاده از همان نام کاربری و
     * رمز عبوری که کاربر برای ثبت‌نام وارد کرده است.
     * <p>
     * در صورت موفقیت، نشست کاربر ثبت شده و کاربر به صفحه‌ی خانه هدایت
     * می‌شود. در صورت شکست ورود خودکار، پیام خطا نمایش داده شده و کاربر به
     * صفحه‌ی ورود هدایت می‌شود تا به‌صورت دستی وارد شود.
     * </p>
     *
     * @param username نام کاربری‌ای که کاربر با آن ثبت‌نام کرده است
     * @param password رمز عبوری که کاربر با آن ثبت‌نام کرده است
     */
    private void autoLogin(String username, String password) {
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
            // ذخیره‌ی session
            SessionManager.getInstance().setSession(
                    response.getToken(),
                    response.getUserId(),
                    response.getUsername(),
                    response.getRole(),
                    response.getName()
            );
            AlertUtil.showSuccess("ثبت‌نام و ورود با موفقیت انجام شد. خوش آمدید " + response.getName());
            // رفتن به صفحه‌ی اصلی
            SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "خانه");
        });

        loginTask.setOnFailed(event -> {
            Throwable ex = loginTask.getException();
            String errorMessage = (ex instanceof ApiException) ? ex.getMessage() : "خطا در ورود خودکار. لطفاً دوباره لاگین کنید.";
            AlertUtil.showError(errorMessage);
            // در صورت خطا، کاربر را به صفحه‌ی لاگین هدایت می‌کنیم تا دستی وارد شود
            SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
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
     * پردازش کلیک روی لینک ورود، و هدایت کاربر به صفحه‌ی ورود.
     */
    @FXML
    private void onLoginLinkClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
    }
}
