package com.groupeisi.minisystemebancaire.config;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration pour l'API Laravel
 */
public class ApiConfig {

    // URL de base de votre API Laravel
    private static final String BASE_URL = "http://localhost:8000"; // Changez selon votre config Laravel

    // Token d'authentification (sera défini après connexion)
    private static String authToken = null;

    // Utilisateur connecté
    private static Long currentUserId = null;

    public static String getApiUrl() {
        return BASE_URL;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static Long getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(Long userId) {
        currentUserId = userId;
    }

    /**
     * Crée les headers avec le token d'authentification
     */
    public static Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        if (authToken != null) {
            headers.put("Authorization", "Bearer " + authToken);
        }

        return headers;
    }

    /**
     * Crée un RequestBody JSON pour les requêtes POST/PUT
     */
    public static RequestBody createJsonRequestBody(String json) {
        return RequestBody.create(json, MediaType.get("application/json"));
    }

    /**
     * Déconnexion - supprime le token et l'utilisateur
     */
    public static void logout() {
        authToken = null;
        currentUserId = null;
    }

    /**
     * Vérifie si l'utilisateur est connecté
     */
    public static boolean isLoggedIn() {
        return authToken != null && currentUserId != null;
    }
}