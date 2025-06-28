package com.groupeisi.minisystemebancaire.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.fatboyindustrial.gsonjavatime.Converters;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LaravelApiService {
    private static final String BASE_URL = "http://localhost:8000/api/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Gson gson;
    private String authToken;

    public LaravelApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))
                .build();

        this.gson = Converters.registerLocalDateTime(new GsonBuilder())
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    // Méthodes d'authentification
    public CompletableFuture<String> login(String email, String password, String userType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = userType.equals("client") ? "clients/login" : "admins/login";

                Map<String, String> loginData = Map.of(
                        "email", email,
                        "password", password
                );

                String response = post(endpoint, loginData);
                Map<String, Object> result = gson.fromJson(response, Map.class);

                if (result.containsKey("token")) {
                    this.authToken = (String) result.get("token");
                    return this.authToken;
                }

                throw new RuntimeException("Token non reçu");
            } catch (Exception e) {
                throw new RuntimeException("Erreur de connexion: " + e.getMessage(), e);
            }
        });
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public void clearAuthToken() {
        this.authToken = null;
    }

    // Méthodes HTTP génériques
    public String get(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + (authToken != null ? authToken : ""))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur HTTP: " + response.code() + " - " + response.message());
            }
            return response.body().string();
        }
    }

    public String post(String endpoint, Object data) throws IOException {
        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + (authToken != null ? authToken : ""))
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur HTTP: " + response.code() + " - " + response.message());
            }
            return response.body().string();
        }
    }

    public String put(String endpoint, Object data) throws IOException {
        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + (authToken != null ? authToken : ""))
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur HTTP: " + response.code() + " - " + response.message());
            }
            return response.body().string();
        }
    }

    public String delete(String endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + (authToken != null ? authToken : ""))
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur HTTP: " + response.code() + " - " + response.message());
            }
            return response.body().string();
        }
    }

    // Méthodes utilitaires pour la désérialisation
    public <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public <T> List<T> fromJsonList(String json, Class<T> classOfT) {
        Type listType = TypeToken.getParameterized(List.class, classOfT).getType();
        return gson.fromJson(json, listType);
    }

    // Singleton pattern
    private static LaravelApiService instance;

    public static LaravelApiService getInstance() {
        if (instance == null) {
            instance = new LaravelApiService();
        }
        return instance;
    }
}