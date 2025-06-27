package com.groupeisi.minisystemebancaire.services;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.util.LocalDateTimeTypeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

public class CompteService {

    /* ---------- CONFIG  GSON ---------- */
    private final Gson gson;

    {
        // Adaptateur perso pour le format « yyyy-MM-dd HH:mm:ss »
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        new LocalDateTimeTypeAdapter())
                .create();
    }
    /* ----------------------------------- */

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String BASE_URL = "http://localhost:8000/api/comptes";

    /* ---------- CRUD  COMPTES ---------- */

    // Créer
    public void createCompte(CompteDTO compteDTO) {
        try {
            String json = gson.toJson(compteDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 201 && resp.statusCode() != 200) {
                throw new RuntimeException(
                        "Erreur création compte : " + resp.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API createCompte", e);
        }
    }

    // Lire par id
    public CompteDTO getCompteById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .GET().build();

            HttpResponse<String> resp =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                return gson.fromJson(resp.body(), CompteDTO.class);
            }
            throw new RuntimeException("Compte non trouvé !");
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getCompteById", e);
        }
    }

    // Lire par numéro
    public CompteDTO getCompteByNumero(String numero) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/numero/" + numero))
                    .GET().build();

            HttpResponse<String> resp =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                return gson.fromJson(resp.body(), CompteDTO.class);
            }
            throw new RuntimeException("Compte " + numero + " non trouvé !");
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getCompteByNumero", e);
        }
    }

    // Lire par client
    public List<CompteDTO> getComptesByClientId(Long clientId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/client/" + clientId))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("API /client/" + clientId + " → "
                        + response.statusCode() + " : " + response.body());
            }

            JsonElement root = JsonParser.parseString(response.body());

            if (root.isJsonArray()) {                              // cas idéal
                return gson.fromJson(root,
                        new TypeToken<List<CompteDTO>>() {}.getType());
            }
            if (root.isJsonObject()) {                             // cas « objet enveloppe »
                JsonObject obj = root.getAsJsonObject();
                if (obj.has("data") && obj.get("data").isJsonArray()) {
                    return gson.fromJson(obj.get("data"),
                            new TypeToken<List<CompteDTO>>() {}.getType());
                }
                if (obj.has("message")) {                          // message d’erreur
                    throw new RuntimeException("API /client/" + clientId + " → "
                            + obj.get("message").getAsString());
                }
            }

            throw new RuntimeException("Format JSON inattendu : " + response.body());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erreur réseau getComptesByClientId", e);
        }
    }


    // Update
    public void updateCompte(Long id, CompteDTO dto) {
        try {
            String json = gson.toJson(dto);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                throw new RuntimeException(
                        "Erreur MAJ compte : " + resp.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API updateCompte", e);
        }
    }

    // Delete
    public void deleteCompte(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE().build();

            HttpResponse<String> resp =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200 && resp.statusCode() != 204) {
                throw new RuntimeException(
                        "Erreur suppression compte : " + resp.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API deleteCompte", e);
        }
    }

    // Lire tous
    public List<CompteDTO> getAllComptes() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Accept", "application/json")
                    .GET().build();

            HttpResponse<String> resp =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                throw new RuntimeException("API /comptes → "
                        + resp.statusCode() + " : " + resp.body());
            }

            JsonElement root = JsonParser.parseString(resp.body());
            if (root.isJsonArray()) {
                return gson.fromJson(root,
                        new TypeToken<List<CompteDTO>>() {}.getType());
            }
            if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                if (obj.has("data") && obj.get("data").isJsonArray()) {
                    return gson.fromJson(obj.get("data"),
                            new TypeToken<List<CompteDTO>>() {}.getType());
                }
                if (obj.has("message")) {
                    throw new RuntimeException("API /comptes → "
                            + obj.get("message").getAsString());
                }
            }
            throw new RuntimeException("Format JSON inattendu : " + resp.body());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erreur réseau getAllComptes", e);
        }
    }

    /* ---------- OPÉRATIONS ANNEXES ---------- */

    public void appliquerFrais(Long compteId, double montant) {
        try {
            String body = gson.toJson(new FraisDTO(montant));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + compteId + "/appliquer-frais"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp =
                    httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                throw new RuntimeException("Erreur application frais !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API appliquerFrais", e);
        }
    }

    public void fermerCompte(Long compteId) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + compteId + "/fermer"))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> resp =
                    httpClient.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                throw new RuntimeException("Erreur fermeture compte !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API fermerCompte", e);
        }
    }

    /* DTO interne pour /appliquer-frais */
    private record FraisDTO(double montant) {}
}
