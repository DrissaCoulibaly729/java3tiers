package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;

import java.net.http.HttpRequest;
import java.util.List;

public class CompteService extends ApiService {

    public List<CompteDTO> getAllComptes() {
        try {
            HttpRequest request = createRequest("/comptes").GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<CompteDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des comptes: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la liste des comptes: " + e.getMessage());
        }
    }

    public CompteDTO getCompteById(Long id) {
        try {
            HttpRequest request = createRequest("/comptes/" + id).GET().build();
            return sendRequest(request, CompteDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du compte ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer le compte: " + e.getMessage());
        }
    }

    public List<CompteDTO> getComptesByClient(Long clientId) {
        try {
            HttpRequest request = createRequest("/comptes/client/" + clientId).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<CompteDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des comptes du client: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les comptes du client: " + e.getMessage());
        }
    }

    public List<CompteDTO> getComptesByClientId(Long clientId) {
        return getComptesByClient(clientId);
    }

    public CompteDTO getCompteByNumero(String numero) {
        try {
            HttpRequest request = createRequest("/comptes/numero/" + numero).GET().build();
            return sendRequest(request, CompteDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du compte numéro " + numero + ": " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer le compte: " + e.getMessage());
        }
    }

    public CompteDTO createCompte(CompteDTO compte) {
        try {
            String json = gson.toJson(compte);
            System.out.println("📤 Création compte avec données: " + json);

            HttpRequest request = createRequest("/comptes")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, CompteDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de créer le compte: " + e.getMessage());
        }
    }

    public CompteDTO updateCompte(CompteDTO compte) {
        try {
            String json = gson.toJson(compte);
            HttpRequest request = createRequest("/comptes/" + compte.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, CompteDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le compte: " + e.getMessage());
        }
    }

    public void deleteCompte(Long id) {
        try {
            HttpRequest request = createRequest("/comptes/" + id).DELETE().build();
            sendRequestForString(request);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer le compte: " + e.getMessage());
        }
    }

    public void appliquerFrais(Long compteId, String type, double montant) {
        try {
            FraisRequest fraisRequest = new FraisRequest(type, montant);
            String json = gson.toJson(fraisRequest);
            HttpRequest request = createRequest("/comptes/" + compteId + "/frais")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Frais appliqués avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'application des frais: " + e.getMessage());
            throw new RuntimeException("Impossible d'appliquer les frais: " + e.getMessage());
        }
    }

    public void appliquerFrais(Long compteId, FraisRequest fraisRequest) {
        try {
            String json = gson.toJson(fraisRequest);
            HttpRequest request = createRequest("/comptes/" + compteId + "/frais")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Frais appliqués avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'application des frais: " + e.getMessage());
            throw new RuntimeException("Impossible d'appliquer les frais: " + e.getMessage());
        }
    }

    public void fermerCompte(Long compteId) {
        try {
            HttpRequest request = createRequest("/comptes/" + compteId + "/fermer")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Compte fermé avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la fermeture du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de fermer le compte: " + e.getMessage());
        }
    }

    // Classes internes pour les requêtes
    public static class FraisRequest {
        private final String type;
        private final double montant;

        public FraisRequest(String type, double montant) {
            this.type = type;
            this.montant = montant;
        }

        public String getType() { return type; }
        public double getMontant() { return montant; }
    }
}