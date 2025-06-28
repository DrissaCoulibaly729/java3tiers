package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ClientService extends ApiService {

    public List<ClientDTO> getAllClients() {
        try {
            HttpRequest request = createRequest("/clients").GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<ClientDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des clients: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la liste des clients: " + e.getMessage());
        }
    }

    public ClientDTO getClientById(Long id) {
        try {
            HttpRequest request = createRequest("/clients/" + id).GET().build();
            return sendRequest(request, ClientDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du client ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer le client: " + e.getMessage());
        }
    }

    public ClientDTO createClient(ClientDTO client) {
        try {
            String json = gson.toJson(client);
            System.out.println("📤 Création client avec données: " + json);

            HttpRequest request = createRequest("/clients")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, ClientDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du client: " + e.getMessage());
            throw new RuntimeException("Impossible de créer le client: " + e.getMessage());
        }
    }

    public ClientDTO registerClient(ClientDTO client) {
        try {
            String json = gson.toJson(client);
            System.out.println("📤 Inscription client avec données: " + json);

            HttpRequest request = createRequest("/clients/register")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, ClientDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'inscription du client: " + e.getMessage());
            throw new RuntimeException("Impossible d'inscrire le client: " + e.getMessage());
        }
    }

    public ClientDTO updateClient(ClientDTO client) {
        try {
            String json = gson.toJson(client);
            HttpRequest request = createRequest("/clients/" + client.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, ClientDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du client: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le client: " + e.getMessage());
        }
    }

    public void deleteClient(Long id) {
        try {
            HttpRequest request = createRequest("/clients/" + id).DELETE().build();
            sendRequestForString(request);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du client: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer le client: " + e.getMessage());
        }
    }

    public ClientDTO login(String email, String password) {
        try {
            LoginRequest loginRequest = new LoginRequest(email, password);
            String json = gson.toJson(loginRequest);

            System.out.println("🔐 Tentative de connexion client...");
            System.out.println("📤 JSON envoyé: " + json);

            HttpRequest request = createRequest("/clients/login")
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
                    ClientDTO client = gson.fromJson(response.body(), ClientDTO.class);
                    System.out.println("✅ Connexion client réussie pour: " + client.getEmail());
                    return client;
                } catch (Exception parseException) {
                    System.out.println("❌ Erreur de parsing JSON: " + parseException.getMessage());
                    System.out.println("📄 JSON à parser: " + response.body());
                    throw new RuntimeException("Erreur de parsing de la réponse");
                }
            } else if (response.statusCode() == 401) {
                System.out.println("❌ Identifiants incorrects (401)");
                throw new RuntimeException("Identifiants incorrects");
            } else if (response.statusCode() == 403) {
                System.out.println("❌ Compte suspendu (403)");
                throw new RuntimeException("Compte suspendu");
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

    public void suspendClient(Long id) {
        try {
            HttpRequest request = createRequest("/clients/" + id + "/suspend")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Client suspendu avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suspension du client: " + e.getMessage());
            throw new RuntimeException("Impossible de suspendre le client: " + e.getMessage());
        }
    }

    public void reactivateClient(Long id) {
        try {
            HttpRequest request = createRequest("/clients/" + id + "/reactivate")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Client réactivé avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la réactivation du client: " + e.getMessage());
            throw new RuntimeException("Impossible de réactiver le client: " + e.getMessage());
        }
    }

    // Classe interne pour les requêtes de connexion
    private static class LoginRequest {
        private final String email;
        private final String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() { return email; }
        public String getPassword() { return password; }
    }
}