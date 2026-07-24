package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.Role;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * <h2>DashboardController</h2>
 * <p>
 * کنترلر صفحه‌ی <b>داشبورد</b> کاربر واردشده. پیام خوش‌آمدگویی شخصی‌سازی‌شده
 * را نمایش داده و بسته به نقش کاربر جاری، دکمه‌ی دسترسی به پنل ادمین را
 * نمایش یا پنهان می‌کند.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.util.SessionManager
 */
public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button adminPanelButton;

    /**
     * مقداردهی اولیه‌ی صفحه پس از بارگذاری FXML.
     * <p>
     * متن خوش‌آمدگویی بر اساس نام کاربری کاربر جاری تنظیم می‌شود و دکمه‌ی
     * پنل ادمین فقط برای کاربرانی با نقش {@link Role#ADMIN} قابل مشاهده و
     * فعال است.
     * </p>
     *
     * @param location  آدرس مورد استفاده برای تفکیک مسیرهای نسبی در فایل FXML (استفاده‌نشده)
     * @param resources منابع بین‌المللی‌سازی (استفاده‌نشده)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String username = SessionManager.getInstance().getUsername();
        if (username != null) {
            welcomeLabel.setText("خوش آمدید، " + username + " 👋");
        } else {
            welcomeLabel.setText("خوش آمدید!");
        }

        Role role = SessionManager.getInstance().getRole();
        adminPanelButton.setVisible(role == Role.ADMIN);
        adminPanelButton.setManaged(role == Role.ADMIN);
    }

    /**
     * پردازش کلیک روی دکمه‌ی خروج از حساب کاربری: پاک کردن نشست جاری و
     * هدایت کاربر به صفحه‌ی ورود.
     */
    @FXML
    private void onLogoutButtonClick() {
        SessionManager.getInstance().clearSession();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
        AlertUtil.showSuccess("با موفقیت خارج شدید.");
    }

    /**
     * پردازش کلیک روی دکمه‌ی ثبت آگهی جدید.
     */
    @FXML
    private void onCreateAdvertisementClick() {
        AlertUtil.showSuccess("صفحه‌ی ثبت آگهی به زودی اضافه می‌شود!");
    }

    /**
     * پردازش کلیک روی دکمه‌ی آگهی‌های من، و هدایت کاربر به صفحه‌ی گفت‌وگوها.
     */
    @FXML
    private void onMyAdvertisementsClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/conversation-list.fxml", "گفتگوها");
    }

    /**
     * پردازش کلیک روی دکمه‌ی پنل ادمین، و هدایت کاربر به صفحه‌ی پنل مدیریت.
     */
    @FXML
    private void onAdminPanelClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/admin-panel.fxml", "پنل ادمین");
    }
}
