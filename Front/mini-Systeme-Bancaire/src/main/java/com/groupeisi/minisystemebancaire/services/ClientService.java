package com.groupeisi.minisystemebancaire.services;

import com.google.gson.JsonObject;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ClientService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String BASE_URL = "http://localhost:8000/api/clients";

    // ✅ Inscription d'un nouveau client
    public void registerClient(ClientDTO clientDTO) {
        try {
            String json = gson.toJson(clientDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/register"))
                    .header("Accept", "application/json") 
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201 && response.statusCode() != 200) {
                throw new RuntimeException("Erreur lors de l'enregistrement : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API registerClient", e);
        }
    }

    // ✅ Connexion d'un client
    public ClientDTO login(String email, String password) {
        try {
            // 1️⃣ on construit un petit JsonObject « à la main »
            JsonObject json = new JsonObject();
            json.addProperty("email", email);
            json.addProperty("password", password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), ClientDTO.class);
            }

            // 2️⃣ Meilleur message d’erreur : on renvoie la réponse brute
            throw new RuntimeException("Login KO (" + response.statusCode() + ") : "
                    + response.body());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erreur réseau login", e);
        }
    }


    // ✅ Lire : Récupérer un client par ID
    public ClientDTO getClientById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), ClientDTO.class);
            } else {
                throw new RuntimeException("Client non trouvé !");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getClientById", e);
        }
    }

    // ✅ Lire : Récupérer tous les clients
    public List<ClientDTO> getAllClients() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return gson.fromJson(response.body(), new TypeToken<List<ClientDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getAllClients", e);
        }
    }

    // ✅ Mettre à jour un client
    public void updateClient(Long id, ClientDTO clientDTO) {
        try {
            String json = gson.toJson(clientDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Accept", "application/json") 
.header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur lors de la mise à jour : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API updateClient", e);
        }
    }

    // ✅ Supprimer un client (seulement si pas de compte actif)
    public void deleteClient(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 204) {
                throw new RuntimeException("Erreur lors de la suppression : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API deleteClient", e);
        }
    }

    public void suspendClient(Long clientId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + clientId + "/suspend"))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur suspension : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API suspendClient", e);
        }
    }

    // ✅ Réactivation d'un client suspendu
    public void reactivateClient(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id + "/reactivate"))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur réactivation : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API reactivateClient", e);
        }
    }
}
