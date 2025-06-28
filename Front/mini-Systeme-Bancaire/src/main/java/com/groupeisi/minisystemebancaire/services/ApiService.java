package com.groupeisi.minisystemebancaire.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fatboyindustrial.gsonjavatime.Converters;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class ApiService {
    // ✅ Configuration de l'URL de base - Assurez-vous que votre backend Laravel fonctionne sur ce port
    protected static final String BASE_URL = "http://localhost:8000/api";

    protected static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    protected static final Gson gson = Converters.registerAll(new GsonBuilder())
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    protected HttpRequest.Builder createRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30));
    }

    protected <T> T sendRequest(HttpRequest request, Class<T> responseType) {
        try {
            System.out.println("🔄 Envoi de la requête vers: " + request.uri());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (response.body() == null || response.body().trim().isEmpty()) {
                    return null;
                }
                return gson.fromJson(response.body(), responseType);
            } else if (response.statusCode() == 401) {
                throw new RuntimeException("Identifiants incorrects");
            } else if (response.statusCode() == 403) {
                throw new RuntimeException("Compte suspendu ou accès interdit");
            } else if (response.statusCode() == 404) {
                throw new RuntimeException("Ressource non trouvée");
            } else if (response.statusCode() == 500) {
                throw new RuntimeException("Erreur serveur interne");
            } else {
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }
        } catch (java.net.ConnectException e) {
            throw new RuntimeException("Impossible de se connecter au serveur. Vérifiez que le backend Laravel est démarré sur http://localhost:8000");
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erreur de communication réseau: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Requête interrompue: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("Erreur de communication avec l'API: " + e.getMessage(), e);
        }
    }

    protected String sendRequestForString(HttpRequest request) {
        try {
            System.out.println("🔄 Envoi de la requête vers: " + request.uri());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else if (response.statusCode() == 401) {
                throw new RuntimeException("Identifiants incorrects");
            } else if (response.statusCode() == 403) {
                throw new RuntimeException("Compte suspendu ou accès interdit");
            } else if (response.statusCode() == 404) {
                throw new RuntimeException("Ressource non trouvée");
            } else if (response.statusCode() == 500) {
                throw new RuntimeException("Erreur serveur interne");
            } else {
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }
        } catch (java.net.ConnectException e) {
            throw new RuntimeException("Impossible de se connecter au serveur. Vérifiez que le backend Laravel est démarré sur http://localhost:8000");
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erreur de communication réseau: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Requête interrompue: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("Erreur de communication avec l'API: " + e.getMessage(), e);
        }
    }

    // ✅ Méthode utilitaire pour tester la connexion
    public static boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/test"))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() < 500;
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
}