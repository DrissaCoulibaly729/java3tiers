package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;

import java.net.http.HttpRequest;
import java.util.List;

public class CompteService extends ApiService {

    public List<CompteDTO> getAllComptes() {
        HttpRequest request = createRequest("/comptes").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<CompteDTO>>(){}.getType());
    }

    public CompteDTO getCompteById(Long id) {
        HttpRequest request = createRequest("/comptes/" + id).GET().build();
        return sendRequest(request, CompteDTO.class);
    }

    public List<CompteDTO> getComptesByClientId(Long clientId) {
        HttpRequest request = createRequest("/comptes/client/" + clientId).GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<CompteDTO>>(){}.getType());
    }

    public CompteDTO getCompteByNumero(String numero) {
        HttpRequest request = createRequest("/comptes/numero/" + numero).GET().build();
        return sendRequest(request, CompteDTO.class);
    }

    public CompteDTO createCompte(CompteDTO compte) {
        String json = gson.toJson(compte);
        HttpRequest request = createRequest("/comptes")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, CompteDTO.class);
    }

    public CompteDTO updateCompte(CompteDTO compte) {
        String json = gson.toJson(compte);
        HttpRequest request = createRequest("/comptes/" + compte.getId())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, CompteDTO.class);
    }

    public void deleteCompte(Long id) {
        HttpRequest request = createRequest("/comptes/" + id).DELETE().build();
        sendRequestForString(request);
    }

    public void appliquerFrais(Long compteId, String typeFrais, Double montant) {
        String json = gson.toJson(new FraisRequest(typeFrais, montant));
        HttpRequest request = createRequest("/comptes/" + compteId + "/frais")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        sendRequestForString(request);
    }

    public void fermerCompte(Long id) {
        HttpRequest request = createRequest("/comptes/" + id + "/fermer")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRequestForString(request);
    }

    private static class FraisRequest {
        private final String type;
        private final Double montant;

        public FraisRequest(String type, Double montant) {
            this.type = type;
            this.montant = montant;
        }
    }
}