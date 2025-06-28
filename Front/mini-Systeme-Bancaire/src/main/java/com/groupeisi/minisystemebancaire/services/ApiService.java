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
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return gson.fromJson(response.body(), responseType);
            } else {
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur de communication avec l'API: " + e.getMessage(), e);
        }
    }

    protected String sendRequestForString(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur de communication avec l'API: " + e.getMessage(), e);
        }
    }
}
