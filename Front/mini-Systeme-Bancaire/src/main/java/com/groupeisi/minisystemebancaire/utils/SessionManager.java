package com.groupeisi.minisystemebancaire.utils;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

public class SessionManager {

    private static AdminDTO currentAdmin;
    private static ClientDTO currentClient;
    private static String currentUserType; // "ADMIN" ou "CLIENT"

    // ✅ Gestion de la session Admin
    public static void setCurrentAdmin(AdminDTO admin) {
        currentAdmin = admin;
        currentClient = null; // Effacer la session client si elle existe
        currentUserType = "ADMIN";
        System.out.println("🔧 Session admin définie pour: " + admin.getUsername());
    }

    public static AdminDTO getCurrentAdmin() {
        return currentAdmin;
    }

    public static void clearCurrentAdmin() {
        currentAdmin = null;
        if ("ADMIN".equals(currentUserType)) {
            currentUserType = null;
        }
        System.out.println("🧹 Session admin effacée");
    }

    public static boolean isAdminLoggedIn() {
        return currentAdmin != null;
    }

    // ✅ Gestion de la session Client
    public static void setCurrentClient(ClientDTO client) {
        currentClient = client;
        currentAdmin = null; // Effacer la session admin si elle existe
        currentUserType = "CLIENT";
        System.out.println("🔧 Session client définie pour: " + client.getEmail());
    }

    public static ClientDTO getCurrentClient() {
        return currentClient;
    }

    public static void clearCurrentClient() {
        currentClient = null;
        if ("CLIENT".equals(currentUserType)) {
            currentUserType = null;
        }
        System.out.println("🧹 Session client effacée");
    }

    public static boolean isClientLoggedIn() {
        return currentClient != null;
    }

    // ✅ AJOUT: Méthodes manquantes utilisées dans votre code

    /**
     * Méthode clearSession() utilisée dans MainApp.java
     */
    public static void clearSession() {
        clearAllSessions();
    }

    /**
     * Méthode logout() utilisée dans les contrôleurs Dashboard
     */
    public static void logout() {
        clearAllSessions();
    }

    // ✅ Gestion générale de la session
    public static String getCurrentUserType() {
        return currentUserType;
    }

    public static boolean isAnyUserLoggedIn() {
        return isAdminLoggedIn() || isClientLoggedIn();
    }

    public static void clearAllSessions() {
        currentAdmin = null;
        currentClient = null;
        currentUserType = null;
        System.out.println("🧹 Toutes les sessions effacées");
    }

    // ✅ Utilitaires pour obtenir des informations sur l'utilisateur connecté
    public static String getCurrentUserName() {
        if (isAdminLoggedIn()) {
            return currentAdmin.getUsername();
        } else if (isClientLoggedIn()) {
            return currentClient.getNom() + " " + currentClient.getPrenom();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        if (isAdminLoggedIn()) {
            return currentAdmin.getId();
        } else if (isClientLoggedIn()) {
            return currentClient.getId();
        }
        return null;
    }

    // ✅ Vérifications de sécurité
    public static void requireAdminSession() throws SecurityException {
        if (!isAdminLoggedIn()) {
            throw new SecurityException("Session administrateur requise");
        }
    }

    public static void requireClientSession() throws SecurityException {
        if (!isClientLoggedIn()) {
            throw new SecurityException("Session client requise");
        }
    }

    public static void requireAnySession() throws SecurityException {
        if (!isAnyUserLoggedIn()) {
            throw new SecurityException("Session utilisateur requise");
        }
    }
}