// This class is a Singleton (private constructor, only one static instance
// is allowed), so @NoArgsConstructor/@AllArgsConstructor must not be used —
// those annotations auto-generate a public constructor, which bypasses the
// exact restriction the private constructor was meant to enforce (it would
// allow creating multiple parallel session instances).
// @Setter is also not appropriate here, since session changes must only go
// through the controlled setSession/clearSession methods, not by setting
// individual fields independently and out of sync with each other.

package com.example.secondhandfx.util;

import com.example.secondhandfx.model.Role;

public class SessionManager {

    private static final SessionManager instance = new SessionManager();

    private String token;
    private Long userId;
    private String username;
    private Role role;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public void setSession(String token, Long userId, String username, Role role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public void clearSession() {
        this.token = null;
        this.userId = null;
        this.username = null;
        this.role = null;
    }

    public boolean isLoggedIn() {
        return token != null;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }
}