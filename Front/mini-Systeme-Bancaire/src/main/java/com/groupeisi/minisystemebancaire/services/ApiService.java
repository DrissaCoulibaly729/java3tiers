package com.groupeisi.minisystemebancaire.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ✅ Service de base pour les appels API - Version simplifiée
 */
public abstract class ApiService {

    protected static final String BASE_URL = "http://localhost:8000/api";
    protected final HttpClient httpClient;
    protected final Gson gson;

    // ✅ Client HTTP statique pour les méthodes statiques
    private static final HttpClient staticHttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // ✅ Configuration Gson SIMPLIFIÉE pour éviter les problèmes LocalDateTime
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    // ===== MÉTHODES STATIQUES =====

    /**
     * ✅ MÉTHODE STATIQUE REQUISE PAR MainController
     * Tester la connexion avec l'API (méthode statique)
     */
    public static boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/clients")) // Test avec une route qui existe
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = staticHttpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Considérer comme disponible si le status est < 500 (même si 401, 404, etc.)
            boolean available = response.statusCode() < 500;

            System.out.println("🔌 Test de connexion API - Status: " + response.statusCode() +
                    " (" + (available ? "Disponible" : "Indisponible") + ")");

            return available;

        } catch (java.net.ConnectException e) {
            System.err.println("❌ Test de connexion échoué (Connexion refusée): Serveur Laravel probablement arrêté");
            return false;
        } catch (java.io.IOException e) {
            System.err.println("❌ Test de connexion échoué (IO): " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Test de connexion interrompu: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }

    // ===== MÉTHODES D'INSTANCE =====

    /**
     * Créer une requête HTTP de base
     */
    protected HttpRequest.Builder createRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    /**
     * Envoyer une requête et récupérer la réponse sous forme de String
     */
    protected String sendRequestForString(HttpRequest request) throws IOException, InterruptedException {
        System.out.println("🔄 Envoi de la requête vers: " + request.uri());

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
        System.out.println("📄 Corps de la réponse: " + response.body());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            String errorMessage = "Erreur API: " + response.statusCode();
            if (response.body() != null && !response.body().isEmpty()) {
                errorMessage += " - " + response.body();
            }
            throw new RuntimeException(errorMessage);
        }
    }

    /**
     * Envoyer une requête et désérialiser la réponse
     */
    protected <T> T sendRequest(HttpRequest request, Class<T> responseType) throws IOException, InterruptedException {
        String responseBody = sendRequestForString(request);

        try {
            return gson.fromJson(responseBody, responseType);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la désérialisation: " + e.getMessage());
            System.err.println("📄 JSON reçu: " + responseBody);
            throw new RuntimeException("Erreur de désérialisation: " + e.getMessage(), e);
        }
    }

    /**
     * Envoyer une requête POST avec données JSON
     */
    protected String sendPostRequest(String endpoint, Object data) throws IOException, InterruptedException {
        String json = gson.toJson(data);

        HttpRequest request = createRequest(endpoint)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return sendRequestForString(request);
    }

    /**
     * Envoyer une requête PUT avec données JSON
     */
    protected String sendPutRequest(String endpoint, Object data) throws IOException, InterruptedException {
        String json = gson.toJson(data);

        HttpRequest request = createRequest(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return sendRequestForString(request);
    }

    /**
     * Envoyer une requête DELETE
     */
    protected String sendDeleteRequest(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = createRequest(endpoint)
                .DELETE()
                .build();

        return sendRequestForString(request);
    }

    /**
     * Vérifier la connectivité avec l'API (méthode d'instance)
     */
    public boolean isApiAvailable() {
        try {
            HttpRequest request = createRequest("/clients").GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 500;
        } catch (Exception e) {
            System.err.println("⚠️ API non disponible: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tester la connexion avec l'API et afficher le résultat
     */
    public void testConnectionWithLog() {
        System.out.println("🔌 Test de connexion à l'API...");
        boolean available = isApiAvailable();
        if (available) {
            System.out.println("✅ Connexion à l'API réussie");
        } else {
            System.out.println("❌ Impossible de se connecter à l'API");
            System.out.println("💡 Vérifiez que le backend Laravel est démarré (php artisan serve)");
        }
    }

    /**
     * Obtenir des informations de debug sur l'API
     */
    public void printApiInfo() {
        System.out.println("🔗 Base URL: " + BASE_URL);
        System.out.println("🔌 API disponible: " + (isApiAvailable() ? "✅ OUI" : "❌ NON"));
        System.out.println("🕐 Timeout: 30 secondes");
    }

    /**
     * Envoyer une requête simple pour tester l'API
     */
    public String testApi() {
        try {
            HttpRequest request = createRequest("/clients")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 500) {
                return "API disponible - Status: " + response.statusCode();
            } else {
                return "API répond mais erreur - Status: " + response.statusCode();
            }
        } catch (Exception e) {
            return "API non disponible - Erreur: " + e.getMessage();
        }
    }

    /**
     * Gérer les erreurs communes de l'API
     */
    protected void handleApiError(int statusCode, String responseBody) {
        switch (statusCode) {
            case 400:
                throw new RuntimeException("Requête invalide: " + responseBody);
            case 401:
                throw new RuntimeException("Non autorisé: " + responseBody);
            case 403:
                throw new RuntimeException("Accès interdit: " + responseBody);
            case 404:
                throw new RuntimeException("Ressource non trouvée: " + responseBody);
            case 422:
                throw new RuntimeException("Données invalides: " + responseBody);
            case 500:
                throw new RuntimeException("Erreur serveur interne");
            default:
                throw new RuntimeException("Erreur API " + statusCode + ": " + responseBody);
        }
    }
}