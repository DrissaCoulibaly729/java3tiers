package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.dtos.ClientDTo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientService {

    /**
     * Obtenir un client par son ID
     */
    public static CompletableFuture<ClientDTo> getClientById(Long clientId) {
        return HttpService.getAsync("/api/clients/" + clientId, ClientDTo.class);
    }

    /**
     * Mettre à jour le profil client
     */
    public static CompletableFuture<ClientDTo> updateClient(Long clientId, ClientDTo client) {
        return HttpService.putAsync("/api/clients/" + clientId, client, ClientDTo.class);
    }

    /**
     * Obtenir tous les clients (pour admin)
     */
    public static CompletableFuture<List<ClientDTo>> getAllClients() {
        return HttpService.getListAsync("/api/clients", ClientDTo.class);
    }

    /**
     * Rechercher des clients
     */
    public static CompletableFuture<List<ClientDTo>> searchClients(String query) {
        return HttpService.getListAsync("/api/clients/search?q=" + query, ClientDTo.class);
    }

    /**
     * Désactiver/Activer un client
     */
    public static CompletableFuture<ClientDTo> toggleClientStatus(Long clientId) {
        return HttpService.putAsync("/api/clients/" + clientId + "/toggle-status", null, ClientDTo.class);
    }
}
