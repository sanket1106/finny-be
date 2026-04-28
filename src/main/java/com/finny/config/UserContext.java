package com.finny.config;

public class UserContext {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    public static String getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void setCurrentUser(String userId) {
        CURRENT_USER.set(userId);
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
