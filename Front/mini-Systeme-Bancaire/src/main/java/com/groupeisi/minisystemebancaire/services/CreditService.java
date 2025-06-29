package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.CreditDTO;
import java.net.http.HttpRequest;
import java.util.List;

public class CreditService extends ApiService {

    public List<CreditDTO> getAllCredits() {
        try {
            HttpRequest request = createRequest("/credits").GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<CreditDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des crédits: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la liste des crédits: " + e.getMessage());
        }
    }

    public CreditDTO getCreditById(Long id) {
        try {
            HttpRequest request = createRequest("/credits/" + id).GET().build();
            return sendRequest(request, CreditDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du crédit: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer le crédit: " + e.getMessage());
        }
    }

    public List<CreditDTO> getCreditsByClient(Long clientId) {
        try {
            HttpRequest request = createRequest("/credits/client/" + clientId).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<CreditDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des crédits du client: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les crédits du client: " + e.getMessage());
        }
    }

    public List<CreditDTO> getCreditsByStatut(String statut) {
        try {
            HttpRequest request = createRequest("/credits/statut/" + statut).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<CreditDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des crédits par statut: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les crédits: " + e.getMessage());
        }
    }

    public CreditDTO createCredit(CreditDTO credit) {
        try {
            String json = gson.toJson(credit);
            HttpRequest request = createRequest("/credits")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, CreditDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du crédit: " + e.getMessage());
            throw new RuntimeException("Impossible de créer le crédit: " + e.getMessage());
        }
    }

    public CreditDTO updateCredit(CreditDTO credit) {
        try {
            String json = gson.toJson(credit);
            HttpRequest request = createRequest("/credits/" + credit.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, CreditDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du crédit: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le crédit: " + e.getMessage());
        }
    }

    public void deleteCredit(Long id) {
        try {
            HttpRequest request = createRequest("/credits/" + id).DELETE().build();
            sendRequestForString(request);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du crédit: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer le crédit: " + e.getMessage());
        }
    }

    public void approuverCredit(Long id) {
        try {
            HttpRequest request = createRequest("/credits/" + id + "/approuver")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Crédit approuvé avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'approbation du crédit: " + e.getMessage());
            throw new RuntimeException("Impossible d'approuver le crédit: " + e.getMessage());
        }
    }

    public void rejeterCredit(Long id) {
        try {
            HttpRequest request = createRequest("/credits/" + id + "/rejeter")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Crédit rejeté avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du rejet du crédit: " + e.getMessage());
            throw new RuntimeException("Impossible de rejeter le crédit: " + e.getMessage());
        }
    }
}