package com.groupeisi.minisystemebancaire.utils;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

/**
 * ✅ Gestionnaire de session pour stocker les informations de l'utilisateur connecté
 */
public class SessionManager {
    private static AdminDTO currentAdmin;
    private static ClientDTO currentClient;
    private static String userType; // "ADMIN" ou "CLIENT"

    // Méthodes pour Admin
    public static void setCurrentAdmin(AdminDTO admin) {
        currentAdmin = admin;
        currentClient = null; // S'assurer qu'un seul type d'utilisateur est connecté
        userType = "ADMIN";
        System.out.println("✅ Session admin créée pour: " + admin.getUsername());
    }

    public static AdminDTO getCurrentAdmin() {
        return currentAdmin;
    }

    public static boolean isAdminLoggedIn() {
        return currentAdmin != null && "ADMIN".equals(userType);
    }

    // Méthodes pour Client
    public static void setCurrentClient(ClientDTO client) {
        currentClient = client;
        currentAdmin = null; // S'assurer qu'un seul type d'utilisateur est connecté
        userType = "CLIENT";
        System.out.println("✅ Session client créée pour: " + client.getEmail());
    }

    public static ClientDTO getCurrentClient() {
        return currentClient;
    }

    public static boolean isClientLoggedIn() {
        return currentClient != null && "CLIENT".equals(userType);
    }

    // Méthodes génériques
    public static String getUserType() {
        return userType;
    }

    public static boolean isLoggedIn() {
        return isAdminLoggedIn() || isClientLoggedIn();
    }

    public static String getCurrentUserName() {
        if (isAdminLoggedIn()) {
            return currentAdmin.getUsername();
        } else if (isClientLoggedIn()) {
            return currentClient.getNom() + " " + currentClient.getPrenom();
        }
        return "Invité";
    }

    public static Long getCurrentUserId() {
        if (isAdminLoggedIn()) {
            return currentAdmin.getId();
        } else if (isClientLoggedIn()) {
            return currentClient.getId();
        }
        return null;
    }

    // Nettoyer la session
    public static void clearSession() {
        System.out.println("🔐 Nettoyage de la session...");
        currentAdmin = null;
        currentClient = null;
        userType = null;
    }

    // Méthode utilitaire pour la déconnexion
    public static void logout() {
        System.out.println("👋 Déconnexion de l'utilisateur: " + getCurrentUserName());
        clearSession();
    }
}