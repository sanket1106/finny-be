package com.finny.config;

public class UserContext {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static String getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void setCurrentUser(String userId) {
        CURRENT_USER.set(userId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static void clear() {
        CURRENT_USER.remove();
        CURRENT_TENANT.remove();
    }
}
