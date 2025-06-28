package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.ClientDTo;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientService {
    private final LaravelApiService apiService = LaravelApiService.getInstance();

    public CompletableFuture<List<ClientDTo>> getAllClients() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = apiService.get(ApiConfig.Endpoints.CLIENTS);
                return apiService.fromJsonList(response, ClientDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du chargement des clients", e);
            }
        });
    }

    public CompletableFuture<ClientDTo> getClientById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = apiService.get(ApiConfig.Endpoints.CLIENTS + "/" + id);
                return apiService.fromJson(response, ClientDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du chargement du client", e);
            }
        });
    }

    public CompletableFuture<ClientDTo> createClient(ClientDTo client) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = apiService.post(ApiConfig.Endpoints.CLIENTS, client);
                return apiService.fromJson(response, ClientDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la création du client", e);
            }
        });
    }

    public CompletableFuture<ClientDTo> updateClient(Long id, ClientDTo client) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = apiService.put(ApiConfig.Endpoints.CLIENTS + "/" + id, client);
                return apiService.fromJson(response, ClientDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la mise à jour du client", e);
            }
        });
    }

    public CompletableFuture<Void> suspendClient(Long id) {
        return CompletableFuture.runAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.CLIENT_SUSPEND, id);
                apiService.put(endpoint, null);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la suspension du client", e);
            }
        });
    }

    public CompletableFuture<Void> reactivateClient(Long id) {
        return CompletableFuture.runAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.CLIENT_REACTIVATE, id);
                apiService.put(endpoint, null);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la réactivation du client", e);
            }
        });
    }
}