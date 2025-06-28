package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.CompteDTo;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompteService {
    private final LaravelApiService apiService = LaravelApiService.getInstance();

    public CompletableFuture<List<CompteDTo>> getComptesByClient(Long clientId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.COMPTES_BY_CLIENT, clientId);
                String response = apiService.get(endpoint);
                return apiService.fromJsonList(response, CompteDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du chargement des comptes", e);
            }
        });
    }

    public CompletableFuture<CompteDTo> getCompteByNumero(String numeroCompte) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.COMPTES_BY_NUMERO, numeroCompte);
                String response = apiService.get(endpoint);
                return apiService.fromJson(response, CompteDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors du chargement du compte", e);
            }
        });
    }

    public CompletableFuture<CompteDTo> createCompte(CompteDTo compte) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = apiService.post(ApiConfig.Endpoints.COMPTES, compte);
                return apiService.fromJson(response, CompteDTo.class);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la création du compte", e);
            }
        });
    }

    public CompletableFuture<Void> fermerCompte(Long compteId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String endpoint = String.format(ApiConfig.Endpoints.COMPTES_FERMER, compteId);
                apiService.put(endpoint, null);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la fermeture du compte", e);
            }
        });
    }
}