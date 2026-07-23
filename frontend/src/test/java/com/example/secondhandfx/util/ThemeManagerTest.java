package com.example.secondhandfx.util;

import javafx.scene.Scene;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ThemeManagerTest {

    // NOTE: ThemeManager.applyTheme(scene) no-ops safely when scene is null
    // (`if (scene == null) return;`), so these tests exercise the theme-state
    // logic (get/set/toggle) without needing a running JavaFX Application
    // thread or a real Scene instance.

    @Test
    void getCurrentTheme_shouldNeverReturnNull() {
        assertNotNull(ThemeManager.getCurrentTheme());
    }

    @Test
    void setTheme_shouldUpdateCurrentThemeToLight() {
        ThemeManager.setTheme(ThemeManager.Theme.LIGHT, null);

        assertEquals(ThemeManager.Theme.LIGHT, ThemeManager.getCurrentTheme());
    }

    @Test
    void setTheme_shouldUpdateCurrentThemeToDark() {
        ThemeManager.setTheme(ThemeManager.Theme.DARK, null);

        assertEquals(ThemeManager.Theme.DARK, ThemeManager.getCurrentTheme());
    }

    @Test
    void toggleTheme_shouldSwitchFromLightToDark() {
        ThemeManager.setTheme(ThemeManager.Theme.LIGHT, null);

        ThemeManager.toggleTheme(null);

        assertEquals(ThemeManager.Theme.DARK, ThemeManager.getCurrentTheme());
    }

    @Test
    void toggleTheme_shouldSwitchFromDarkToLight() {
        ThemeManager.setTheme(ThemeManager.Theme.DARK, null);

        ThemeManager.toggleTheme(null);

        assertEquals(ThemeManager.Theme.LIGHT, ThemeManager.getCurrentTheme());
    }

    @Test
    void toggleTheme_calledTwice_shouldReturnToOriginalTheme() {
        ThemeManager.setTheme(ThemeManager.Theme.LIGHT, null);

        ThemeManager.toggleTheme(null);
        ThemeManager.toggleTheme(null);

        assertEquals(ThemeManager.Theme.LIGHT, ThemeManager.getCurrentTheme());
    }

    @Test
    void applyTheme_shouldNotThrow_whenSceneIsNull() {
        //چون دو تا متد استاتیک از applyTheme وجود دارد صریح مشخص شد
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> ThemeManager.applyTheme((Scene) null));
    }
}
