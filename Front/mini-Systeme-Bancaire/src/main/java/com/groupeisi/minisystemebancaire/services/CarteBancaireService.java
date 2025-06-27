package com.groupeisi.minisystemebancaire.services;

import com.google.gson.*;
import com.groupeisi.minisystemebancaire.models.CarteBancaire;
import com.groupeisi.minisystemebancaire.dto.CarteBancaireDTO;
import com.groupeisi.minisystemebancaire.mappers.CarteBancaireMapper;
import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.util.LocalDateTimeTypeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

public class CarteBancaireService {

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

    private final String BASE_URL = "http://localhost:8000/api/carte-bancaires";

    // ✅ Récupérer une carte par son numéro
    public CarteBancaireDTO getCarteByNumero(String numeroCarte) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/numero/" + numeroCarte))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), CarteBancaireDTO.class);
            } else {
                throw new RuntimeException("Carte non trouvée !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getCarteByNumero", e);
        }
    }

    /**
     * ✅ Créer une carte bancaire pour un client
     */
    public void demanderCarte(CarteBancaireDTO carteDTO) {
        try {
            String json = gson.toJson(carteDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201 && response.statusCode() != 200) {
                throw new RuntimeException("Erreur création de carte : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API demanderCarte", e);
        }
    }

    /**
     * ✅ Lire : Récupérer une carte par ID
     */
    public CarteBancaireDTO getCarteById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), CarteBancaireDTO.class);
            } else {
                throw new RuntimeException("Carte non trouvée !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getCarteById", e);
        }
    }

    /**
     * ✅ Lire : Récupérer toutes les cartes d'un compte
     */
    public List<CarteBancaireDTO> getCartesByCompte(Long compteId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/compte/" + compteId))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return gson.fromJson(response.body(), new TypeToken<List<CarteBancaireDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getCartesByCompte", e);
        }
    }

    /**
     * ✅ Lire : Récupérer toutes les cartes bancaires
     */
    public List<CarteBancaireDTO> getAllCartes() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))            // ← vérifie cette constante
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 1. Code HTTP
            if (response.statusCode() != 200) {
                throw new RuntimeException("API /cartes → " + response.statusCode()
                        + " : " + response.body());
            }

            // 2. Corps
            String json = response.body().trim();
            System.out.println("Réponse brute : " + json);

            JsonElement root = JsonParser.parseString(json);

            if (root.isJsonArray()) {
                // Cas [ {...}, {...} ]
                return gson.fromJson(root,
                        new TypeToken<List<CarteBancaireDTO>>() {}.getType());
            }

            if (root.isJsonObject() && root.getAsJsonObject().has("data")) {
                // Cas { "data": [ ... ] }
                return gson.fromJson(root.getAsJsonObject().get("data"),
                        new TypeToken<List<CarteBancaireDTO>>() {}.getType());
            }

            throw new RuntimeException("Format JSON inattendu : " + json);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erreur réseau getAllCartes", e);
        }
    }



    /**
     * ✅ Modifier une carte (Ex: Mise à jour du statut)
     */
    public void updateCarte(CarteBancaireDTO carteDTO) {
        try {
            String json = gson.toJson(carteDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + carteDTO.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur mise à jour carte : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API updateCarte", e);
        }
    }

    // ✅ Bloquer ou Débloquer une carte
    public void updateCarteStatut(Long id, String statut) {
        try {
            CarteBancaireDTO dto = getCarteById(id);
            dto.setStatut(statut);
            updateCarte(dto);
        } catch (Exception e) {
            throw new RuntimeException("Erreur API updateCarteStatut", e);
        }
    }

    /**
     * ✅ Bloquer une carte bancaire
     */
    public void bloquerCarte(Long id) {
        updateCarteStatut(id, "Bloquée");
    }

    /**
     * ✅ Débloquer une carte bancaire
     */
    public void debloquerCarte(Long id) {
        updateCarteStatut(id, "Active");
    }

    /**
     * ✅ Vérifier si une carte est valide (ex: non expirée)
     */
    public boolean isCarteValide(Long id) {
        CarteBancaireDTO dto = getCarteById(id);
        return !dto.getStatut().equalsIgnoreCase("Expirée");
    }

    /**
     * ✅ Supprimer une carte bancaire si elle est expirée
     */
    public void deleteCarte(Long id) {
        CarteBancaireDTO dto = getCarteById(id);
        if (!dto.getStatut().equalsIgnoreCase("Expirée")) {
            throw new RuntimeException("Impossible de supprimer une carte active !");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204 && response.statusCode() != 200) {
                throw new RuntimeException("Erreur suppression : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API deleteCarte", e);
        }
    }

    // ✅ Ajout de la méthode pour récupérer toutes les cartes d’un client
    public List<CarteBancaireDTO> getCartesByClient(Long clientId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/client/" + clientId))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return gson.fromJson(response.body(), new TypeToken<List<CarteBancaireDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getCartesByClient", e);
        }
    }
}
