package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

import java.net.http.HttpRequest;
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
            System.out.println("📤 Données envoyées: " + json);

            HttpRequest request = createRequest("/clients/login")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            ClientDTO client = sendRequest(request, ClientDTO.class);

            if (client != null) {
                System.out.println("✅ Connexion client réussie pour: " + client.getEmail());
            }

            return client;
        } catch (Exception e) {
            System.err.println("❌ Identifiants incorrects ou compte suspendu");
            throw new RuntimeException("Identifiants incorrects ou compte suspendu");
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