package com.groupeisi.minisystemebancaire.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ApiConfig {
    private static final String API_BASE_URL = "http://localhost:8080/api";
    @Getter @Setter
    private static String authToken;

    public static String getApiUrl() {
        return API_BASE_URL;
    }

    public static HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null && !authToken.isEmpty()) {
            headers.setBearerAuth(authToken);
        }
        return headers;
    }

    public static void clearAuthToken() {
        authToken = null;
    }
}
