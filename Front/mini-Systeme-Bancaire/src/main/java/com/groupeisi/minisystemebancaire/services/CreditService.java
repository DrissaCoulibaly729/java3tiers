package com.groupeisi.minisystemebancaire.services;

import com.google.gson.GsonBuilder;
import com.groupeisi.minisystemebancaire.dto.CreditDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.util.LocalDateTimeTypeAdapter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

public class CreditService {

    /* ---------- CONFIG  GSON ---------- */
    private final Gson gson;

    {
        // Adaptateur perso pour le format « yyyy-MM-dd HH:mm:ss »
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        new LocalDateTimeTypeAdapter())
                .create();
    }

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String BASE_URL = "http://localhost:8000/api/credits";

    // ✅ Demander un crédit
    public void demanderCredit(CreditDTO creditDTO) {
        try {
            creditDTO.setStatut("En attente");
            String json = gson.toJson(creditDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Accept", "application/json") 
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201) {
                throw new RuntimeException("Erreur création crédit : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API demanderCredit", e);
        }
    }

    // ✅ Lire : Récupérer un crédit par ID
    public CreditDTO getCreditById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), CreditDTO.class);
            } else {
                throw new RuntimeException("Crédit non trouvé !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getCreditById", e);
        }
    }

    // ✅ Lire : Récupérer tous les crédits d'un client
    public List<CreditDTO> getCreditsByClient(Long clientId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/client/" + clientId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<CreditDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getCreditsByClient", e);
        }
    }

    // ✅ Lire : Récupérer les crédits par statut
    public List<CreditDTO> getCreditsByStatut(String statut) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/statut/" + statut))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<CreditDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getCreditsByStatut", e);
        }
    }

    // ✅ Accepter un crédit
    public void accepterCredit(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id + "/accepter"))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur acceptation crédit !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API accepterCredit", e);
        }
    }

    // ✅ Refuser un crédit
    public void refuserCredit(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id + "/refuser"))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur refus crédit !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API refuserCredit", e);
        }
    }

    // ✅ Modifier un crédit
    public void updateCredit(Long id, CreditDTO creditDTO) {
        try {
            String json = gson.toJson(creditDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Accept", "application/json") 
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur mise à jour crédit !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API updateCredit", e);
        }
    }

    // ✅ Supprimer un crédit
    public void deleteCredit(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 204) {
                throw new RuntimeException("Erreur suppression crédit !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API deleteCredit", e);
        }
    }

    // ✅ Lire : Récupérer tous les crédits
    public List<CreditDTO> getAllCredits() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<CreditDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getAllCredits", e);
        }
    }
}
