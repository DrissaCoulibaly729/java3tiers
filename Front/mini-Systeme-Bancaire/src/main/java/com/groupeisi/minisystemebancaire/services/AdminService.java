package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.models.Admin;
import com.groupeisi.minisystemebancaire.mappers.AdminMapper;
import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AdminService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String BASE_URL = "http://localhost:8000/api/admins";

    /**
     * ✅ Authentifie un administrateur par son username et mot de passe
     */
    public AdminDTO authentifierAdmin(String username, String password) {
        try {
            String json = gson.toJson(new AdminDTO(null, username, password, null));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), AdminDTO.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ Authentification de l'admin
    public AdminDTO login(String username, String password) {
        try {
            String json = gson.toJson(new AdminDTO(null, username, password, null));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), AdminDTO.class);
            } else {
                throw new RuntimeException("Erreur d'authentification : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API login", e);
        }
    }

    // ✅ Créer un nouvel admin
    public void createAdmin(AdminDTO adminDTO) {
        try {
            String json = gson.toJson(adminDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 201 && response.statusCode() != 200) {
                throw new RuntimeException("Erreur création : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API createAdmin", e);
        }
    }

    // ✅ Lire : Récupérer un admin par ID
    public AdminDTO getAdminById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), AdminDTO.class);
            } else {
                throw new RuntimeException("Admin non trouvé");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getAdminById", e);
        }
    }

    // ✅ Lire : Récupérer tous les admins
    public List<AdminDTO> getAllAdmins() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), new TypeToken<List<AdminDTO>>() {}.getType());
            } else {
                throw new RuntimeException("Erreur récupération des admins : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getAllAdmins", e);
        }
    }

    // ✅ Mettre à jour un admin (changement de rôle, mot de passe)
    public void updateAdmin(Long id, AdminDTO adminDTO) {
        try {
            String json = gson.toJson(adminDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur mise à jour : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API updateAdmin", e);
        }
    }

    // ✅ Supprimer un admin (Sauf si c'est le dernier admin)
    public void deleteAdmin(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 204) {
                throw new RuntimeException("Erreur suppression : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API deleteAdmin", e);
        }
    }
}
