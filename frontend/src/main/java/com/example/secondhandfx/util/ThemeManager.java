package com.example.secondhandfx.util;

import javafx.scene.Scene;

import java.util.prefs.Preferences;

public class ThemeManager {

    public enum Theme { LIGHT, DARK }

    private static final String COMPONENTS_CSS = "/css/components.css";
    private static final String LIGHT_CSS = "/css/theme-light.css";
    private static final String DARK_CSS = "/css/theme-dark.css";
    private static final String PREF_KEY = "app_theme";

    private static final Preferences preferences = Preferences.userNodeForPackage(ThemeManager.class);
    private static Theme currentTheme = Theme.valueOf(preferences.get(PREF_KEY, Theme.LIGHT.name()));

    private ThemeManager() {
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static void toggleTheme(Scene scene) {
        setTheme(currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT, scene);
    }

    public static void setTheme(Theme theme, Scene scene) {
        currentTheme = theme;
        preferences.put(PREF_KEY, theme.name());
        applyTheme(scene);
    }

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
}