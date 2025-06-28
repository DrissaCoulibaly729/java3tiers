package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.dto.RemboursementDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class RemboursementService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String BASE_URL = "http://localhost:8000/api/remboursements";

    // ✅ Enregistrer un remboursement
    public void enregistrerRemboursement(RemboursementDTO remboursementDTO) {
        try {
            String json = gson.toJson(remboursementDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new RuntimeException("Erreur d'enregistrement : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API enregistrerRemboursement", e);
        }
    }

    // ✅ Lire : Récupérer un remboursement par ID
    public RemboursementDTO getRemboursementById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), RemboursementDTO.class);
            } else {
                throw new RuntimeException("Remboursement non trouvé !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getRemboursementById", e);
        }
    }

    // ✅ Lire : Récupérer tous les remboursements d'un crédit
    public List<RemboursementDTO> getRemboursementsByCredit(Long creditId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/credit/" + creditId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<RemboursementDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getRemboursementsByCredit", e);
        }
    }

    // ✅ Modifier un remboursement (changer la date par exemple)
    public void updateRemboursement(Long id, String nouvelleDate) {
        try {
            String json = "{\"date\":\"" + nouvelleDate + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur mise à jour du remboursement !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API updateRemboursement", e);
        }
    }

    // ✅ Supprimer un remboursement
    public void deleteRemboursement(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 204) {
                throw new RuntimeException("Erreur suppression remboursement !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API deleteRemboursement", e);
        }
    }

    // ✅ Lire : Récupérer tous les remboursements
    public List<RemboursementDTO> getAllRemboursements() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<RemboursementDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getAllRemboursements", e);
        }
    }
}
