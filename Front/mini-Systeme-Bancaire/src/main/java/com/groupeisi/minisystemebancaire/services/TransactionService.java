package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.TransactionDTo;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransactionService {
    private final LaravelApiService apiService = LaravelApiService.getInstance();

    public CompletableFuture<List<TransactionDTo>> getTransactionsByClient(Long clientId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.TRANSACTIONS_BY_CLIENT, clientId);
                String response = apiService.get(endpoint);
                return apiService.fromJsonList(response, TransactionDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du chargement des transactions", e);
            }
        });
    }

    public CompletableFuture<List<TransactionDTo>> getTransactionsByCompte(Long compteId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.TRANSACTIONS_BY_COMPTE, compteId);
                String response = apiService.get(endpoint);
                return apiService.fromJsonList(response, TransactionDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du chargement des transactions", e);
            }
        });
    }

    public CompletableFuture<TransactionDTo> createTransaction(TransactionDTo transaction) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = apiService.post(ApiConfig.Endpoints.TRANSACTIONS, transaction);
                return apiService.fromJson(response, TransactionDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la création de la transaction", e);
            }
        });
    }

    public CompletableFuture<List<TransactionDTo>> getTransactionsSuspectes() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = apiService.get(ApiConfig.Endpoints.TRANSACTIONS_SUSPECTES);
                return apiService.fromJsonList(response, TransactionDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du chargement des transactions suspectes", e);
            }
        });
    }
}