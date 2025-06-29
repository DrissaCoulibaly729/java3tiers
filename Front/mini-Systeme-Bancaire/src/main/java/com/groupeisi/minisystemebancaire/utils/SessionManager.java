package com.groupeisi.minisystemebancaire.utils;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

public class SessionManager {

    private static AdminDTO currentAdmin;
    private static ClientDTO currentClient;
    private static String currentUserType; // "ADMIN" ou "CLIENT"

    // ✅ Gestion de la session Admin
    public static void setCurrentAdmin(AdminDTO admin) {
        if (admin == null) {
            System.err.println("⚠️ Tentative de définir un admin null en session");
            return;
        }

        if (admin.getId() == null) {
            System.err.println("❌ ERREUR CRITIQUE : Tentative de sauvegarder un admin sans ID !");
            System.err.println("👤 Username: " + admin.getUsername());
            throw new IllegalArgumentException("L'admin doit avoir un ID valide");
        }

        currentAdmin = admin;
        currentClient = null; // Effacer la session client si elle existe
        currentUserType = "ADMIN";
        System.out.println("🔧 Session admin définie pour: " + admin.getUsername() + " (ID: " + admin.getId() + ")");
    }

    public static AdminDTO getCurrentAdmin() {
        if (currentAdmin != null && currentAdmin.getId() == null) {
            System.err.println("❌ ERREUR : Admin en session sans ID - Nettoyage de la session");
            clearCurrentAdmin();
            return null;
        }
        return currentAdmin;
    }

    public static void clearCurrentAdmin() {
        if (currentAdmin != null) {
            System.out.println("🧹 Session admin effacée pour: " + currentAdmin.getUsername());
        }
        currentAdmin = null;
        if ("ADMIN".equals(currentUserType)) {
            currentUserType = null;
        }
    }

    public static boolean isAdminLoggedIn() {
        return currentAdmin != null && currentAdmin.getId() != null;
    }

    // ✅ Gestion de la session Client avec vérifications renforcées
    public static void setCurrentClient(ClientDTO client) {
        if (client == null) {
            System.err.println("⚠️ Tentative de définir un client null en session");
            return;
        }

        if (client.getId() == null) {
            System.err.println("❌ ERREUR CRITIQUE : Tentative de sauvegarder un client sans ID !");
            System.err.println("📧 Email du client: " + client.getEmail());
            System.err.println("👤 Nom: " + client.getNom() + " " + client.getPrenom());
            throw new IllegalArgumentException("Le client doit avoir un ID valide");
        }

        currentClient = client;
        currentAdmin = null; // Effacer la session admin si elle existe
        currentUserType = "CLIENT";
        System.out.println("🔧 Session client définie pour: " + client.getEmail() + " (ID: " + client.getId() + ")");
    }

    public static ClientDTO getCurrentClient() {
        if (currentClient != null && currentClient.getId() == null) {
            System.err.println("❌ ERREUR : Client en session sans ID - Nettoyage de la session");
            clearCurrentClient();
            return null;
        }
        return currentClient;
    }

    public static void clearCurrentClient() {
        if (currentClient != null) {
            System.out.println("🧹 Session client effacée pour: " + currentClient.getEmail());
        }
        currentClient = null;
        if ("CLIENT".equals(currentUserType)) {
            currentUserType = null;
        }
    }

    public static boolean isClientLoggedIn() {
        return currentClient != null && currentClient.getId() != null;
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
        if (currentAdmin != null || currentClient != null) {
            System.out.println("🧹 Toutes les sessions effacées");
        }
        currentAdmin = null;
        currentClient = null;
        currentUserType = null;
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

    public static String getCurrentUserEmail() {
        if (isAdminLoggedIn()) {
            return currentAdmin.getUsername(); // Ou email si disponible
        } else if (isClientLoggedIn()) {
            return currentClient.getEmail();
        }
        return null;
    }

    // ✅ Méthode de diagnostic pour debug
    public static void printSessionDiagnostic() {
        System.out.println("🔍 === DIAGNOSTIC SESSION ===");
        System.out.println("Type utilisateur: " + currentUserType);

        if (currentAdmin != null) {
            System.out.println("Admin: " + currentAdmin.getUsername() + " (ID: " + currentAdmin.getId() + ")");
        } else {
            System.out.println("Admin: null");
        }

        if (currentClient != null) {
            System.out.println("Client: " + currentClient.getEmail() + " (ID: " + currentClient.getId() + ")");
        } else {
            System.out.println("Client: null");
        }

        System.out.println("Session valide: " + isAnyUserLoggedIn());
        System.out.println("=== FIN DIAGNOSTIC ===");
    }

    // ✅ Vérifications de sécurité avec diagnostic
    public static void requireAdminSession() throws SecurityException {
        if (!isAdminLoggedIn()) {
            System.err.println("❌ Session administrateur requise mais non trouvée");
            printSessionDiagnostic();
            throw new SecurityException("Session administrateur requise");
        }

        if (currentAdmin.getId() == null) {
            System.err.println("❌ Session admin corrompue (ID manquant)");
            clearCurrentAdmin();
            throw new SecurityException("Session administrateur corrompue");
        }
    }

    public static void requireClientSession() throws SecurityException {
        if (!isClientLoggedIn()) {
            System.err.println("❌ Session client requise mais non trouvée");
            printSessionDiagnostic();
            throw new SecurityException("Session client requise");
        }

        if (currentClient.getId() == null) {
            System.err.println("❌ Session client corrompue (ID manquant)");
            clearCurrentClient();
            throw new SecurityException("Session client corrompue");
        }
    }

    public static void requireAnySession() throws SecurityException {
        if (!isAnyUserLoggedIn()) {
            System.err.println("❌ Session utilisateur requise mais non trouvée");
            printSessionDiagnostic();
            throw new SecurityException("Session utilisateur requise");
        }
    }

    // ✅ Méthodes de validation
    public static boolean validateCurrentSession() {
        try {
            if (isAdminLoggedIn()) {
                requireAdminSession();
                return true;
            } else if (isClientLoggedIn()) {
                requireClientSession();
                return true;
            }
            return false;
        } catch (SecurityException e) {
            System.err.println("⚠️ Session invalide détectée: " + e.getMessage());
            return false;
        }
    }

    // ✅ Méthodes d'information sur la session
    public static String getSessionInfo() {
        if (isAdminLoggedIn()) {
            return "Admin: " + currentAdmin.getUsername() + " (ID: " + currentAdmin.getId() + ")";
        } else if (isClientLoggedIn()) {
            return "Client: " + currentClient.getEmail() + " (ID: " + currentClient.getId() + ")";
        }
        return "Aucune session active";
    }

    public static boolean hasValidSession() {
        return validateCurrentSession();
    }

    // ✅ Méthode pour rafraîchir la session client (utile après mise à jour des données)
    public static void refreshClientSession(ClientDTO updatedClient) {
        if (isClientLoggedIn() && updatedClient != null &&
                currentClient.getId().equals(updatedClient.getId())) {
            System.out.println("🔄 Rafraîchissement de la session client");
            setCurrentClient(updatedClient);
        }
    }

    // ✅ Méthode pour rafraîchir la session admin
    public static void refreshAdminSession(AdminDTO updatedAdmin) {
        if (isAdminLoggedIn() && updatedAdmin != null &&
                currentAdmin.getId().equals(updatedAdmin.getId())) {
            System.out.println("🔄 Rafraîchissement de la session admin");
            setCurrentAdmin(updatedAdmin);
        }
    }
}