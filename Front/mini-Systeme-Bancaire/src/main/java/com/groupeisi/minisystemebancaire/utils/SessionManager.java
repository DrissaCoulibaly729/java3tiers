package com.groupeisi.minisystemebancaire.utils;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

public class SessionManager {
    private static ClientDTO currentClient;
    private static AdminDTO currentAdmin;
    private static String userType; // "CLIENT" ou "ADMIN"

    // Gestion session client
    public static void setCurrentClient(ClientDTO client) {
        currentClient = client;
        currentAdmin = null;
        userType = "CLIENT";
    }

    public static ClientDTO getCurrentClient() {
        return currentClient;
    }

    // Gestion session admin
    public static void setCurrentAdmin(AdminDTO admin) {
        currentAdmin = admin;
        currentClient = null;
        userType = "ADMIN";
    }

    public static AdminDTO getCurrentAdmin() {
        return currentAdmin;
    }

    // Utilitaires
    public static String getUserType() {
        return userType;
    }

    public static boolean isClientLoggedIn() {
        return currentClient != null && "CLIENT".equals(userType);
    }

    public static boolean isAdminLoggedIn() {
        return currentAdmin != null && "ADMIN".equals(userType);
    }

    public static void clearSession() {
        currentClient = null;
        currentAdmin = null;
        userType = null;
    }

    public static Long getCurrentUserId() {
        if (isClientLoggedIn()) {
            return currentClient.getId();
        } else if (isAdminLoggedIn()) {
            return currentAdmin.getId();
        }
        return null;
    }
}
