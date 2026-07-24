package com.example.secondhandfx.util;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

import java.util.prefs.Preferences;

/**
 * <h2>ThemeManager</h2>
 * <p>
 * کلاس کمکی (Utility) مسئول مدیریت <b>تم روشن/تاریک (Light/Dark)</b> رابط
 * کاربری برنامه. تم انتخابی کاربر با استفاده از {@link Preferences} به‌صورت
 * محلی ذخیره می‌شود تا بین اجراهای مختلف برنامه حفظ شود.
 * </p>
 *
 * @author تیم فرانت‌اند
 */
public class ThemeManager {

    public enum Theme { LIGHT, DARK }

    private static final String COMPONENTS_CSS = "/css/components.css";
    private static final String LIGHT_CSS = "/css/theme-light.css";
    private static final String DARK_CSS = "/css/theme-dark.css";
    private static final String PREF_KEY = "app_theme";

    private static final Preferences preferences = Preferences.userNodeForPackage(ThemeManager.class);
    private static Theme currentTheme = Theme.valueOf(preferences.get(PREF_KEY, Theme.LIGHT.name()));

    /** سازنده‌ی خصوصی برای جلوگیری از نمونه‌سازی؛ این کلاس فقط شامل متدهای استاتیک است. */
    private ThemeManager() {
    }

    /**
     * دریافت تم فعلی برنامه.
     *
     * @return تم جاری ({@link Theme#LIGHT} یا {@link Theme#DARK})
     */
    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    /**
     * تعویض تم فعلی بین حالت روشن و تاریک، و اعمال آن روی صحنه‌ی داده‌شده.
     *
     * @param scene صحنه‌ای که تم جدید باید روی آن اعمال شود
     */
    public static void toggleTheme(Scene scene) {
        setTheme(currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT, scene);
    }

    /**
     * تنظیم تم برنامه به مقدار مشخص، ذخیره‌ی آن در {@link Preferences}، و
     * اعمال آن روی صحنه‌ی داده‌شده.
     *
     * @param theme تم جدیدی که باید اعمال شود
     * @param scene صحنه‌ای که تم جدید باید روی آن اعمال شود
     */
    public static void setTheme(Theme theme, Scene scene) {
        currentTheme = theme;
        preferences.put(PREF_KEY, theme.name());
        applyTheme(scene);
    }

    /**
     * اعمال شیت‌های استایل تم فعلی (روشن یا تاریک) به‌همراه استایل‌های
     * کامپوننت‌های مشترک، روی یک صحنه.
     *
     * @param scene صحنه‌ای که استایل‌ها باید روی آن اعمال شود؛ در صورت
     *              {@code null} بودن، هیچ عملیاتی انجام نمی‌شود
     */
    public static void applyTheme(Scene scene) {
        if (scene == null) {
            return;
        }

        String themeCssPath = currentTheme == Theme.DARK ? DARK_CSS : LIGHT_CSS;

        scene.getStylesheets().setAll(
                ThemeManager.class.getResource(themeCssPath).toExternalForm(),
                ThemeManager.class.getResource(COMPONENTS_CSS).toExternalForm()
        );
    }

    /**
     * دیالوگ‌ها (Dialog/Alert/TextInputDialog) صحنه‌ی جدا و مستقل از پنجره‌ی اصلی دارند،
     * بنابراین استایل‌شیت‌های اعمال‌شده روی Scene اصلی روی آن‌ها اثر نمی‌گذارد و همیشه
     * با ظاهر پیش‌فرض روشن JavaFX نمایش داده می‌شوند. این متد همان تم و کامپوننت‌های
     * جاری را مستقیماً روی DialogPane اعمال می‌کند تا دیالوگ‌ها هم دارک/لایت شوند.
     */
    public static void applyTheme(DialogPane dialogPane) {
        if (dialogPane == null) {
            return;
        }

        String themeCssPath = currentTheme == Theme.DARK ? DARK_CSS : LIGHT_CSS;

        dialogPane.getStylesheets().setAll(
                ThemeManager.class.getResource(themeCssPath).toExternalForm(),
                ThemeManager.class.getResource(COMPONENTS_CSS).toExternalForm()
        );
        if (!dialogPane.getStyleClass().contains("root")) {
            dialogPane.getStyleClass().add("root");
        }
    }
}
