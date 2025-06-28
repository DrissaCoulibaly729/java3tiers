package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.dtos.CompteDTo;
import com.groupeisi.minisystemebancaire.dtos.TransactionDTo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CompteService {

    /**
     * Obtenir tous les comptes d'un client
     */
    public static CompletableFuture<List<CompteDTo>> getComptesByClient(Long clientId) {
        return HttpService.getListAsync("/api/comptes/client/" + clientId, CompteDTo.class);
    }

    /**
     * Obtenir un compte par son ID
     */
    public static CompletableFuture<CompteDTo> getCompteById(Long compteId) {
        return HttpService.getAsync("/api/comptes/" + compteId, CompteDTo.class);
    }

    /**
     * Créer un nouveau compte
     */
    public static CompletableFuture<CompteDTo> creerCompte(String type, Long clientId) {
        Map<String, Object> request = new HashMap<>();
        request.put("type", type);
        request.put("client_id", clientId);

        return HttpService.postAsync("/api/comptes", request, CompteDTo.class);
    }

    /**
     * Effectuer un virement
     */
    public static CompletableFuture<TransactionDTo> effectuerVirement(Long compteSourceId, Long compteDestinationId, BigDecimal montant, String description) {
        Map<String, Object> request = new HashMap<>();
        request.put("compte_source_id", compteSourceId);
        request.put("compte_destination_id", compteDestinationId);
        request.put("montant", montant);
        request.put("description", description);

        return HttpService.postAsync("/api/comptes/virement", request, TransactionDTo.class);
    }

    /**
     * Effectuer un dépôt
     */
    public static CompletableFuture<TransactionDTo> effectuerDepot(Long compteId, BigDecimal montant, String description) {
        Map<String, Object> request = new HashMap<>();
        request.put("compte_id", compteId);
        request.put("montant", montant);
        request.put("description", description);

        return HttpService.postAsync("/api/comptes/depot", request, TransactionDTo.class);
    }

    /**
     * Effectuer un retrait
     */
    public static CompletableFuture<TransactionDTo> effectuerRetrait(Long compteId, BigDecimal montant, String description) {
        Map<String, Object> request = new HashMap<>();
        request.put("compte_id", compteId);
        request.put("montant", montant);
        request.put("description", description);

        return HttpService.postAsync("/api/comptes/retrait", request, TransactionDTo.class);
    }
}