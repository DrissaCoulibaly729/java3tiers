package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.AdminDTO;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AdminService extends ApiService {

    public List<AdminDTO> getAllAdmins() {
        try {
            HttpRequest request = createRequest("/admins").GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<AdminDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des admins: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la liste des administrateurs: " + e.getMessage());
        }
    }

    public AdminDTO getAdminById(Long id) {
        try {
            HttpRequest request = createRequest("/admins/" + id).GET().build();
            return sendRequest(request, AdminDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de l'admin ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer l'administrateur: " + e.getMessage());
        }
    }

    public AdminDTO createAdmin(AdminDTO admin) {
        try {
            String json = gson.toJson(admin);
            System.out.println("📤 Création admin avec données: " + json);

            HttpRequest request = createRequest("/admins")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, AdminDTO.class);
        } catch (Exception e) {
            // Ne pas afficher d'erreur si l'admin existe déjà
            if (e.getMessage().contains("unique") || e.getMessage().contains("already exists")) {
                System.out.println("ℹ️ Admin par défaut existe déjà");
                return null;
            }
            System.err.println("❌ Erreur lors de la création de l'admin: " + e.getMessage());
            throw new RuntimeException("Impossible de créer l'administrateur: " + e.getMessage());
        }
    }

    public AdminDTO updateAdmin(AdminDTO admin) {
        try {
            String json = gson.toJson(admin);
            HttpRequest request = createRequest("/admins/" + admin.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, AdminDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'admin: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour l'administrateur: " + e.getMessage());
        }
    }

    public void deleteAdmin(Long id) {
        try {
            HttpRequest request = createRequest("/admins/" + id).DELETE().build();
            sendRequestForString(request);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression de l'admin: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer l'administrateur: " + e.getMessage());
        }
    }

    public AdminDTO login(String username, String password) {
        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            String json = gson.toJson(loginRequest);

            System.out.println("🔐 Tentative de connexion admin...");
            System.out.println("📤 JSON envoyé: " + json);

            HttpRequest request = createRequest("/admins/login")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // Ajouter plus de logs pour déboguer
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Status: " + response.statusCode());
            System.out.println("📄 Réponse brute: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (response.body() == null || response.body().trim().isEmpty()) {
                    System.out.println("❌ Réponse vide du serveur");
                    return null;
                }

                try {
                    AdminDTO admin = gson.fromJson(response.body(), AdminDTO.class);
                    System.out.println("✅ Connexion admin réussie pour: " + admin.getUsername());
                    return admin;
                } catch (Exception parseException) {
                    System.out.println("❌ Erreur de parsing JSON: " + parseException.getMessage());
                    System.out.println("📄 JSON à parser: " + response.body());
                    throw new RuntimeException("Erreur de parsing de la réponse");
                }
            } else if (response.statusCode() == 401) {
                System.out.println("❌ Identifiants incorrects (401)");
                throw new RuntimeException("Identifiants incorrects");
            } else {
                System.out.println("❌ Erreur serveur: " + response.statusCode() + " - " + response.body());
                throw new RuntimeException("Erreur serveur: " + response.statusCode());
            }

        } catch (java.net.ConnectException e) {
            throw new RuntimeException("Impossible de se connecter au serveur. Vérifiez que le backend Laravel est démarré sur http://localhost:8000");
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erreur de communication réseau: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Requête interrompue: " + e.getMessage(), e);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("Erreur de communication avec l'API: " + e.getMessage(), e);
        }
    }

    // Classe interne pour les requêtes de connexion
    private static class LoginRequest {
        private final String username;
        private final String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}