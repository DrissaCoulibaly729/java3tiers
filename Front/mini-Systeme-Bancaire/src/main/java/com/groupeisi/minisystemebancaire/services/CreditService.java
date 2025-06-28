package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.CreditDTo;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CreditService {
    private final LaravelApiService apiService = LaravelApiService.getInstance();

    public CompletableFuture<List<CreditDTo>> getCreditsByClient(Long clientId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.CREDITS_BY_CLIENT, clientId);
                String response = apiService.get(endpoint);
                return apiService.fromJsonList(response, CreditDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du chargement des crédits", e);
            }
        });
    }

    public CompletableFuture<CreditDTo> createCredit(CreditDTo credit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = apiService.post(ApiConfig.Endpoints.CREDITS, credit);
                return apiService.fromJson(response, CreditDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la demande de crédit", e);
            }
        });
    }

    public CompletableFuture<Void> accepterCredit(Long creditId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.CREDIT_ACCEPTER, creditId);
                apiService.put(endpoint, null);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de l'acceptation du crédit", e);
            }
        });
    }

    public CompletableFuture<Void> refuserCredit(Long creditId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.CREDIT_REFUSER, creditId);
                apiService.put(endpoint, null);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du refus du crédit", e);
            }
        });
    }
}