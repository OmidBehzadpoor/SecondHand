package com.example.secondhandfx.util;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * <h2>AlertUtil</h2>
 * <p>
 * کلاس کمکی (Utility) برای نمایش پیام‌های کوتاه و ناپدیدشونده (<b>Toast</b>)
 * به کاربر در رابط کاربری JavaFX، شامل پیام‌های خطا، موفقیت و اطلاع‌رسانی.
 * پیام‌ها با یک انیمیشن محوشدگی (Fade) پس از چند ثانیه به‌طور خودکار حذف
 * می‌شوند.
 * </p>
 * <p>
 * در صورتی که ظرف نمایش Toast (از طریق {@link SceneNavigator#getToastContainer()})
 * در دسترس نباشد، به نمایش یک {@code Alert} استاندارد JavaFX بازگشت داده می‌شود.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.util.SceneNavigator
 */
public class AlertUtil {

    private static final int TOAST_DURATION_SECONDS = 3;

    /** سازنده‌ی خصوصی برای جلوگیری از نمونه‌سازی؛ این کلاس فقط شامل متدهای استاتیک است. */
    private AlertUtil() {
    }

    /**
     * نمایش یک پیام Toast با استایل خطا.
     *
     * @param message متن پیام خطا
     */
    public static void showError(String message) {
        showToast(message, "toast-error");
    }

    /**
     * نمایش یک پیام Toast با استایل موفقیت.
     *
     * @param message متن پیام موفقیت
     */
    public static void showSuccess(String message) {
        showToast(message, "toast-success");
    }

    /**
     * نمایش یک پیام Toast با استایل اطلاع‌رسانی.
     *
     * @param message متن پیام اطلاع‌رسانی
     */
    public static void showInfo(String message) {
        showToast(message, "toast-info");
    }

    /**
     * نمایش یک پیام Toast با کلاس استایل مشخص، به‌همراه انیمیشن محوشدگی خودکار.
     * <p>
     * در صورتی که ظرف Toast در دسترس نباشد، از {@link #showAlertFallback(String)}
     * استفاده می‌شود.
     * </p>
     *
     * @param message   متن پیام
     * @param typeClass کلاس CSS متناظر با نوع پیام (خطا/موفقیت/اطلاع‌رسانی)
     */
    private static void showToast(String message, String typeClass) {
        Platform.runLater(() -> {
            VBox toastContainer = SceneNavigator.getToastContainer();
            if (toastContainer == null) {
                showAlertFallback(message);
                return;
            }

            HBox toast = createToast(message, typeClass);
            toastContainer.getChildren().add(toast);

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(TOAST_DURATION_SECONDS), toast);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                toastContainer.getChildren().remove(toast);
            });
            fadeOut.play();
        });
    }

    /**
     * ساخت المان بصری (UI) یک پیام Toast.
     *
     * @param message   متن پیام
     * @param typeClass کلاس CSS متناظر با نوع پیام
     * @return یک {@link HBox} حاوی برچسب پیام و استایل‌های مربوطه
     */
    private static HBox createToast(String message, String typeClass) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().addAll("toast-label");

        HBox toast = new HBox(label);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().addAll("toast", typeClass);

        toast.setMaxWidth(400);
        toast.setMouseTransparent(true);
        return toast;
    }

    /**
     * نمایش یک {@code Alert} استاندارد JavaFX به‌عنوان جایگزین، برای زمانی که
     * ظرف نمایش Toast در دسترس نیست.
     *
     * @param message متن پیامی که باید نمایش داده شود
     */
    private static void showAlertFallback(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                message,
                javafx.scene.control.ButtonType.OK
        );
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
