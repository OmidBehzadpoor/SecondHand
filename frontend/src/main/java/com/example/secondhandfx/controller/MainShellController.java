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

/**
 * <h2>MainShellController</h2>
 * <p>
 * کنترلر <b>پوسته‌ی اصلی (Shell)</b> برنامه پس از ورود کاربر، شامل نوار
 * ساید‌بار کشویی برای ناوبری بین بخش‌های مختلف (گفت‌وگوها، آگهی‌های من، ثبت
 * آگهی، علاقه‌مندی‌ها و پنل ادمین) و یک ناحیه‌ی محتوای مرکزی که صفحات
 * مختلف در آن بارگذاری می‌شوند.
 * </p>
 *
 * @author تیم فرانت‌اند
 */
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

    /**
     * مقداردهی اولیه‌ی پوسته‌ی اصلی پس از بارگذاری FXML.
     * <p>
     * پیام خوش‌آمدگویی و نمایان بودن دکمه‌ی پنل ادمین بر اساس اطلاعات کاربر
     * جاری تنظیم می‌شود، ساید‌بار در حالت بسته قرار می‌گیرد، و به‌صورت
     * پیش‌فرض بخش گفت‌وگوها در ناحیه‌ی محتوای مرکزی بارگذاری می‌شود.
     * </p>
     */
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

    /**
     * پردازش کلیک روی دکمه‌ی «☰ منو»: باز یا بسته کردن ساید‌بار با انیمیشن کشویی.
     */
    @FXML
    private void onToggleSidebarClick() {
        sidebarOpen = !sidebarOpen;

        TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, sidebarBox);
        slide.setToX(sidebarOpen ? 0 : -SIDEBAR_WIDTH);
        slide.play();
    }

    /**
     * بستن ساید‌بار (در صورت باز بودن) با انیمیشن کشویی؛ معمولاً پس از
     * انتخاب یکی از گزینه‌های منو فراخوانی می‌شود.
     */
    private void closeSidebar() {
        if (sidebarOpen) {
            sidebarOpen = false;
            TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, sidebarBox);
            slide.setToX(-SIDEBAR_WIDTH);
            slide.play();
        }
    }

    /**
     * بارگذاری یک فایل FXML و جایگزینی آن به‌عنوان محتوای ناحیه‌ی مرکزی
     * (بدون ناوبری کامل صفحه یا تغییر عنوان پنجره).
     *
     * @param fxmlPath مسیر (Classpath) فایل FXML مورد نظر برای بارگذاری
     */
    private void loadIntoContentArea(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            AlertUtil.showError("بارگذاری این بخش با خطا مواجه شد.");
        }
    }

    /**
     * پردازش کلیک روی گزینه‌ی «خانه»، و هدایت کاربر به صفحه‌ی آگهی‌ها.
     */
    @FXML
    private void onHomeClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    /**
     * پردازش کلیک روی گزینه‌ی «آگهی‌های من»: بستن ساید‌بار و هدایت کاربر به
     * صفحه‌ی آگهی‌های خودش.
     */
    @FXML
    private void onMyAdvertisementsClick() {
        closeSidebar();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/my-advertisements.fxml", "آگهی‌های من");
    }

    /**
     * پردازش کلیک روی گزینه‌ی «ثبت آگهی»: بستن ساید‌بار و هدایت کاربر به
     * فرم ثبت آگهی جدید.
     */
    @FXML
    private void onCreateAdvertisementClick() {
        closeSidebar();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/advertisement-form.fxml", "ثبت آگهی جدید");
    }

    /**
     * پردازش کلیک روی گزینه‌ی «علاقه‌مندی‌ها»: بستن ساید‌بار و هدایت کاربر
     * به صفحه‌ی علاقه‌مندی‌های خودش.
     */
    @FXML
    private void onFavoritesClick() {
        closeSidebar();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/favorites.fxml", "علاقه‌مندی‌های من");
    }

    /**
     * پردازش کلیک روی گزینه‌ی «گفت‌وگوها»: بارگذاری صفحه‌ی گفت‌وگوها در
     * ناحیه‌ی محتوای مرکزی و بستن ساید‌بار.
     */
    @FXML
    private void onConversationsClick() {
        loadIntoContentArea("/com/example/secondhandfx/fxml/conversation-list.fxml");
        closeSidebar();
    }

    /**
     * پردازش کلیک روی گزینه‌ی «پنل ادمین»: بارگذاری پنل مدیریت در ناحیه‌ی
     * محتوای مرکزی و بستن ساید‌بار.
     */
    @FXML
    private void onAdminPanelClick() {
        loadIntoContentArea("/com/example/secondhandfx/fxml/admin-panel.fxml");
        closeSidebar();
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
}
