package com.groupeisi.minisystemebancaire.config;

import lombok.Getter;
import lombok.Setter;

public class ApiConfig {
    private static final String API_BASE_URL = "http://localhost:8000/api/";

    @Getter @Setter
    private static String authToken;

    @Getter @Setter
    private static String currentUserType; // "client" ou "admin"

    @Getter @Setter
    private static Long currentUserId;

    public static String getApiUrl() {
        return API_BASE_URL;
    }

    public static String getApiUrl(String endpoint) {
        return API_BASE_URL + endpoint;
    }

    public static boolean isAuthenticated() {
        return authToken != null && !authToken.isEmpty();
    }

    public static boolean isClient() {
        return "client".equals(currentUserType);
    }

    public static boolean isAdmin() {
        return "admin".equals(currentUserType);
    }

    public static void setUserSession(String token, String userType, Long userId) {
        authToken = token;
        currentUserType = userType;
        currentUserId = userId;
    }

    public static void clearSession() {
        authToken = null;
        currentUserType = null;
        currentUserId = null;
    }

    // Endpoints Laravel selon vos routes
    public static class Endpoints {
        // Auth
        public static final String CLIENT_LOGIN = "clients/login";
        public static final String ADMIN_LOGIN = "admins/login";
        public static final String CLIENT_REGISTER = "clients/register";

        // Clients
        public static final String CLIENTS = "clients";
        public static final String CLIENT_SUSPEND = "clients/%d/suspend";
        public static final String CLIENT_REACTIVATE = "clients/%d/reactivate";

        // Comptes
        public static final String COMPTES = "comptes";
        public static final String COMPTES_BY_CLIENT = "comptes/client/%d";
        public static final String COMPTES_BY_NUMERO = "comptes/numero/%s";
        public static final String COMPTES_FRAIS = "comptes/%d/frais";
        public static final String COMPTES_FERMER = "comptes/%d/fermer";

        // Cartes bancaires
        public static final String CARTES = "carte-bancaires";
        public static final String CARTES_BY_COMPTE = "compte/%d/cartes";
        public static final String CARTE_BLOQUER = "carte-bancaires/%d/bloquer";
        public static final String CARTE_DEBLOQUER = "carte-bancaires/%d/debloquer";
        public static final String CARTE_VALIDE = "carte-bancaires/valide/%d";

        // Crédits
        public static final String CREDITS = "credits";
        public static final String CREDIT_ACCEPTER = "credits/%d/accepter";
        public static final String CREDIT_REFUSER = "credits/%d/refuser";
        public static final String CREDITS_BY_CLIENT = "credits/client/%d";
        public static final String CREDITS_BY_STATUT = "credits/statut/%s";

        // Transactions
        public static final String TRANSACTIONS = "transactions";
        public static final String TRANSACTIONS_SUSPECTES = "transactions/suspectes";
        public static final String TRANSACTION_ANNULER = "transactions/%d/annuler";
        public static final String TRANSACTIONS_BY_COMPTE = "comptes/%d/transactions";
        public static final String TRANSACTIONS_BY_CLIENT = "clients/%d/transactions";

        // Support
        public static final String TICKETS = "ticket-supports";
        public static final String TICKETS_BY_CLIENT = "tickets/client/%d";
        public static final String TICKET_RECHERCHE = "ticket-supports/recherche/%s";
        public static final String TICKET_REPONDRE = "ticket-supports/%d/repondre";
        public static final String TICKET_RESOLU = "ticket-supports/%d/resolu";

        // Frais bancaires
        public static final String FRAIS = "frais-bancaires";
        public static final String FRAIS_BY_COMPTE = "comptes/%d/frais";
    }
}