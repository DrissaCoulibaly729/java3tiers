package com.groupeisi.minisystemebancaire.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.config.ApiConfig;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service HTTP pour communiquer avec l'API Laravel
 */
public class HttpService {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    /**
     * Requête GET asynchrone
     */
    public static <T> CompletableFuture<T> getAsync(String endpoint, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return get(endpoint, responseType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Requête GET pour obtenir une liste
     */
    public static <T> CompletableFuture<List<T>> getListAsync(String endpoint, Class<T> itemType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getList(endpoint, itemType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Requête POST asynchrone
     */
    public static <T> CompletableFuture<T> postAsync(String endpoint, Object requestBody, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return post(endpoint, requestBody, responseType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Requête PUT asynchrone
     */
    public static <T> CompletableFuture<T> putAsync(String endpoint, Object requestBody, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return put(endpoint, requestBody, responseType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Requête DELETE asynchrone
     */
    public static CompletableFuture<Boolean> deleteAsync(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return delete(endpoint);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Méthodes synchrones

    public static <T> T get(String endpoint, Class<T> responseType) throws IOException {
        Request request = buildRequest(endpoint)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            return gson.fromJson(responseBody, responseType);
        }
    }

    public static <T> List<T> getList(String endpoint, Class<T> itemType) throws IOException {
        Request request = buildRequest(endpoint)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            Type listType = TypeToken.getParameterized(List.class, itemType).getType();
            return gson.fromJson(responseBody, listType);
        }
    }

    public static <T> T post(String endpoint, Object requestBody, Class<T> responseType) throws IOException {
        String json = gson.toJson(requestBody);
        RequestBody body = ApiConfig.createJsonRequestBody(json);

        Request request = buildRequest(endpoint)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + response.code() + " - " + response.message());
            }

            String responseBodyString = response.body().string();
            return gson.fromJson(responseBodyString, responseType);
        }
    }

    public static <T> T put(String endpoint, Object requestBody, Class<T> responseType) throws IOException {
        String json = gson.toJson(requestBody);
        RequestBody body = ApiConfig.createJsonRequestBody(json);

        Request request = buildRequest(endpoint)
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + response.code() + " - " + response.message());
            }

            String responseBodyString = response.body().string();
            return gson.fromJson(responseBodyString, responseType);
        }
    }

    public static boolean delete(String endpoint) throws IOException {
        Request request = buildRequest(endpoint)
                .delete()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    /**
     * Construit une requête avec les headers appropriés
     */
    private static Request.Builder buildRequest(String endpoint) {
        String url = ApiConfig.getApiUrl() + endpoint;
        Request.Builder builder = new Request.Builder().url(url);

        // Ajouter les headers
        Map<String, String> headers = ApiConfig.createHeaders();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }

        return builder;
    }
}