package com.example.secondhandfx.util;

import com.example.secondhandfx.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    @BeforeEach
    void clearBeforeEach() {
        SessionManager.getInstance().clearSession();
    }

    @AfterEach
    void clearAfterEach() {
        SessionManager.getInstance().clearSession();
    }

    @Test
    void getInstance_shouldAlwaysReturnTheSameInstance() {
        SessionManager first = SessionManager.getInstance();
        SessionManager second = SessionManager.getInstance();

        assertSame(first, second);
    }

    @Test
    void isLoggedIn_shouldReturnFalse_whenNoSessionIsSet() {
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void setSession_shouldStoreAllProvidedFields() {
        SessionManager.getInstance().setSession("jwt-token", 1L, "ali123", Role.USER, "Test User");

        SessionManager session = SessionManager.getInstance();
        assertEquals("jwt-token", session.getToken());
        assertEquals(1L, session.getUserId());
        assertEquals("ali123", session.getUsername());
        assertEquals(Role.USER, session.getRole());
    }

    @Test
    void isLoggedIn_shouldReturnTrue_afterSettingSession() {
        SessionManager.getInstance().setSession("jwt-token", 1L, "ali123", Role.USER, "Test User");

        assertTrue(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void isAdmin_shouldReturnTrue_whenRoleIsAdmin() {
        SessionManager.getInstance().setSession("jwt-token", 1L, "admin1", Role.ADMIN , "Test User");

        assertTrue(SessionManager.getInstance().isAdmin());
    }

    @Test
    void isAdmin_shouldReturnFalse_whenRoleIsUser() {
        SessionManager.getInstance().setSession("jwt-token", 1L, "ali123", Role.USER, "Test User");

        assertFalse(SessionManager.getInstance().isAdmin());
    }

    @Test
    void isAdmin_shouldReturnFalse_whenNoSessionIsSet() {
        assertFalse(SessionManager.getInstance().isAdmin());
    }

    @Test
    void clearSession_shouldResetAllFields() {
        SessionManager.getInstance().setSession("jwt-token", 1L, "ali123", Role.USER, "Test User");

        SessionManager.getInstance().clearSession();

        SessionManager session = SessionManager.getInstance();
        assertNull(session.getToken());
        assertNull(session.getUserId());
        assertNull(session.getUsername());
        assertNull(session.getRole());
        assertFalse(session.isLoggedIn());
    }
}
