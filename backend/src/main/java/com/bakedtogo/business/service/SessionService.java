package com.bakedtogo.business.service;

import com.bakedtogo.business.domain.User;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionService {

    private User currentUser;
    private final Map<String, User> sessions = new HashMap<>();

    public void startSession(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Invalid user");
        }
        currentUser = user;
    }

    public String createToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Invalid user");
        }

        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public User getUserByToken(String token) {
        if (token == null) {
            return null;
        }
        return sessions.get(token);
    }

    public void logout() {
        currentUser = null;
    }

    public void invalidateToken(String token) {
        sessions.remove(token);
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void requireAuthentication() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Login required");
        }
    }
}
