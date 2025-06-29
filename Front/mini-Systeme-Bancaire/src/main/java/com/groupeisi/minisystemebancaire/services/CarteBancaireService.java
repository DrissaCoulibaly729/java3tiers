package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.CarteBancaireDTO;
import java.net.http.HttpRequest;
import java.util.List;

public class CarteBancaireService extends ApiService {

    public List<CarteBancaireDTO> getAllCartes() {
        try {
            HttpRequest request = createRequest("/cartes").GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<CarteBancaireDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des cartes: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la liste des cartes: " + e.getMessage());
        }
    }

    public CarteBancaireDTO getCarteById(Long id) {
        try {
            HttpRequest request = createRequest("/cartes/" + id).GET().build();
            return sendRequest(request, CarteBancaireDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de la carte: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la carte: " + e.getMessage());
        }
    }

    public List<CarteBancaireDTO> getCartesByCompte(Long compteId) {
        try {
            HttpRequest request = createRequest("/cartes/compte/" + compteId).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<CarteBancaireDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des cartes du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les cartes du compte: " + e.getMessage());
        }
    }

    public CarteBancaireDTO createCarte(CarteBancaireDTO carte) {
        try {
            String json = gson.toJson(carte);
            HttpRequest request = createRequest("/cartes")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, CarteBancaireDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de la carte: " + e.getMessage());
            throw new RuntimeException("Impossible de créer la carte: " + e.getMessage());
        }
    }

    public CarteBancaireDTO updateCarte(CarteBancaireDTO carte) {
        try {
            String json = gson.toJson(carte);
            HttpRequest request = createRequest("/cartes/" + carte.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, CarteBancaireDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de la carte: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour la carte: " + e.getMessage());
        }
    }

    public void deleteCarte(Long id) {
        try {
            HttpRequest request = createRequest("/cartes/" + id).DELETE().build();
            sendRequestForString(request);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression de la carte: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer la carte: " + e.getMessage());
        }
    }

    public void bloquerCarte(Long id) {
        try {
            HttpRequest request = createRequest("/cartes/" + id + "/bloquer")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Carte bloquée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du blocage de la carte: " + e.getMessage());
            throw new RuntimeException("Impossible de bloquer la carte: " + e.getMessage());
        }
    }

    public void debloquerCarte(Long id) {
        try {
            HttpRequest request = createRequest("/cartes/" + id + "/debloquer")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Carte débloquée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du déblocage de la carte: " + e.getMessage());
            throw new RuntimeException("Impossible de débloquer la carte: " + e.getMessage());
        }
    }
}