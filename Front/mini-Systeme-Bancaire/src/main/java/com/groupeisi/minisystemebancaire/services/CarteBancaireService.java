package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.CarteBancaireDTO;

import java.net.http.HttpRequest;
import java.util.List;

public class CarteBancaireService extends ApiService {

    public List<CarteBancaireDTO> getAllCartes() {
        HttpRequest request = createRequest("/carte-bancaires").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<CarteBancaireDTO>>(){}.getType());
    }

    public CarteBancaireDTO getCarteById(Long id) {
        HttpRequest request = createRequest("/carte-bancaires/" + id).GET().build();
        return sendRequest(request, CarteBancaireDTO.class);
    }

    public List<CarteBancaireDTO> getCartesByCompte(Long compteId) {
        HttpRequest request = createRequest("/compte/" + compteId + "/cartes").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<CarteBancaireDTO>>(){}.getType());
    }

    public CarteBancaireDTO createCarte(CarteBancaireDTO carte) {
        String json = gson.toJson(carte);
        HttpRequest request = createRequest("/carte-bancaires")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, CarteBancaireDTO.class);
    }

    public CarteBancaireDTO updateCarte(CarteBancaireDTO carte) {
        String json = gson.toJson(carte);
        HttpRequest request = createRequest("/carte-bancaires/" + carte.getId())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, CarteBancaireDTO.class);
    }

    public void deleteCarte(Long id) {
        HttpRequest request = createRequest("/carte-bancaires/" + id).DELETE().build();
        sendRequestForString(request);
    }

    public void bloquerCarte(Long id) {
        HttpRequest request = createRequest("/carte-bancaires/" + id + "/bloquer")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRequestForString(request);
    }

    public void debloquerCarte(Long id) {
        HttpRequest request = createRequest("/carte-bancaires/" + id + "/debloquer")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRequestForString(request);
    }

    public boolean isCarteValide(Long id) {
        HttpRequest request = createRequest("/carte-bancaires/valide/" + id).GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, ValiditeResponse.class).valide;
    }

    private static class ValiditeResponse {
        private boolean valide;
    }
}