package com.example.secondhandfx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * <h2>SceneNavigator</h2>
 * <p>
 * کلاس کمکی (Utility) مسئول <b>ناوبری بین صفحات (Scene) مختلف</b> برنامه‌ی
 * JavaFX. این کلاس مرجع‌های اصلی رابط کاربری (پنجره‌ی اصلی، ظرف محتوای مرکزی،
 * و ظرف نمایش پیام‌های Toast) را نگه‌داری می‌کند و بارگذاری فایل‌های FXML و
 * اعمال تم جاری روی صحنه‌ی جدید را مدیریت می‌کند.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.util.ThemeManager
 * @see com.example.secondhandfx.util.AlertUtil
 */
public class SceneNavigator {

    private static Stage primaryStage;
    private static BorderPane mainContainer;
    private static VBox toastContainer;

    /** سازنده‌ی خصوصی برای جلوگیری از نمونه‌سازی؛ این کلاس فقط شامل متدهای استاتیک است. */
    private SceneNavigator() {
    }

    /**
     * ثبت پنجره‌ی اصلی (Stage) برنامه.
     *
     * @param stage پنجره‌ی اصلی JavaFX
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * ثبت ظرف محتوای مرکزی برنامه که صفحات مختلف در آن بارگذاری می‌شوند.
     *
     * @param container ظرف {@link BorderPane} محتوای مرکزی
     */
    public static void setMainContainer(BorderPane container) {
        mainContainer = container;
    }

    /**
     * ثبت ظرف نمایش پیام‌های Toast.
     *
     * @param container ظرف {@link VBox} نمایش پیام‌های Toast
     */
    public static void setToastContainer(VBox container) {
        toastContainer = container;
    }

    /**
     * دریافت ظرف فعلی نمایش پیام‌های Toast.
     *
     * @return ظرف {@link VBox} ثبت‌شده، یا {@code null} در صورت عدم ثبت
     */
    public static VBox getToastContainer() {
        return toastContainer;
    }

    /**
     * بارگذاری یک فایل FXML و نمایش آن در ظرف محتوای مرکزی، به‌همراه اعمال
     * تم جاری و به‌روزرسانی عنوان پنجره.
     *
     * @param fxmlPath مسیر (Classpath) فایل FXML مورد نظر برای بارگذاری
     * @param title    عنوان جدید پنجره پس از ناوبری
     * @return {@link FXMLLoader} استفاده‌شده برای بارگذاری صفحه، جهت دسترسی به کنترلر آن
     * @throws IllegalStateException در صورت بروز خطا هنگام بارگذاری فایل FXML
     */
    public static FXMLLoader navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Set content in main container
            mainContainer.setCenter(root);

            // Apply theme to the scene (if exists)
            if (primaryStage != null) {
                Scene scene = primaryStage.getScene();
                if (scene != null) {
                    ThemeManager.applyTheme(scene);
                }
                primaryStage.setTitle(title);
            }

            return loader;
        } catch (IOException e) {
            throw new IllegalStateException("بارگذاری صفحه‌ی " + fxmlPath + " با خطا مواجه شد", e);
        }
    }
}
