package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.dtos.TransactionDTo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransactionService {

    /**
     * Obtenir les transactions d'un compte
     */
    public static CompletableFuture<List<TransactionDTo>> getTransactionsByCompte(Long compteId) {
        return HttpService.getListAsync("/api/transactions/compte/" + compteId, TransactionDTo.class);
    }

    /**
     * Obtenir les dernières transactions d'un client
     */
    public static CompletableFuture<List<TransactionDTo>> getDernieresTransactions(Long clientId, int limit) {
        return HttpService.getListAsync("/api/transactions/client/" + clientId + "?limit=" + limit, TransactionDTo.class);
    }

    /**
     * Obtenir une transaction par son ID
     */
    public static CompletableFuture<TransactionDTo> getTransactionById(Long transactionId) {
        return HttpService.getAsync("/api/transactions/" + transactionId, TransactionDTo.class);
    }

    /**
     * Obtenir l'historique des transactions avec filtres
     */
    public static CompletableFuture<List<TransactionDTo>> getHistorique(Long clientId, String dateDebut, String dateFin, String type) {
        StringBuilder url = new StringBuilder("/api/transactions/historique/" + clientId + "?");

        if (dateDebut != null) url.append("date_debut=").append(dateDebut).append("&");
        if (dateFin != null) url.append("date_fin=").append(dateFin).append("&");
        if (type != null) url.append("type=").append(type).append("&");

        return HttpService.getListAsync(url.toString(), TransactionDTo.class);
    }
}