package com.groupeisi.minisystemebancaire.services;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ClientService {
    private static final String BASE_URL = "http://localhost:8000/api";
    private final HttpClient httpClient;
    private final Gson gson;

    public ClientService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        this.gson = Converters.registerOffsetDateTime(new GsonBuilder())
                .setLenient()
                .create();
    }

    private HttpRequest.Builder createRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30));
    }

    private String sendRequestForString(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * ✅ MÉTHODE LOGIN CORRIGÉE
     */
    public ClientDTO login(String email, String password) {
        try {
            System.out.println("🔐 Tentative de connexion client...");

            // Création de l'objet de connexion
            LoginRequest loginRequest = new LoginRequest(email, password);
            String json = gson.toJson(loginRequest);
            System.out.println("📤 JSON envoyé: " + json);

            HttpRequest request = createRequest("/clients/login")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Status de connexion client: " + response.statusCode());
            System.out.println("📄 Réponse brute de connexion client: " + response.body());

            if (response.statusCode() == 200) {
                // ✅ CORRECTION : Parser la réponse complète
                LoginResponse loginResponse = gson.fromJson(response.body(), LoginResponse.class);

                if (loginResponse != null && loginResponse.isSuccess() && loginResponse.getClient() != null) {
                    ClientDTO client = loginResponse.getClient();

                    // ✅ VÉRIFICATION CRITIQUE : S'assurer que l'ID existe
                    if (client.getId() == null) {
                        System.err.println("❌ ERREUR : Client retourné sans ID !");
                        throw new RuntimeException("Le serveur n'a pas retourné l'ID du client");
                    }

                    System.out.println("✅ Connexion client réussie pour: " + client.getEmail() + " (ID: " + client.getId() + ")");
                    return client;
                } else {
                    System.err.println("❌ Réponse de connexion invalide ou échec");
                    return null;
                }
            } else {
                System.err.println("❌ Échec de la connexion - Status: " + response.statusCode());
                if (response.statusCode() == 401) {
                    throw new RuntimeException("Email ou mot de passe incorrect");
                } else {
                    throw new RuntimeException("Erreur serveur: " + response.statusCode());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la connexion client: " + e.getMessage());
            throw new RuntimeException("Impossible de se connecter: " + e.getMessage());
        }
    }

    /**
     * Inscription d'un nouveau client
     */
    public ClientDTO registerClient(ClientDTO client) {
        try {
            System.out.println("📝 Tentative d'inscription client: " + client.getEmail());

            String json = gson.toJson(client);
            System.out.println("📤 Inscription client avec données: " + json);

            HttpRequest request = createRequest("/clients/register")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ClientDTO clientCree = gson.fromJson(response.body(), ClientDTO.class);
                System.out.println("✅ Client inscrit avec succès: " + clientCree.getEmail());
                return clientCree;
            } else {
                System.err.println("❌ Erreur lors de l'inscription - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'inscription du client: " + e.getMessage());
            throw new RuntimeException("Impossible d'inscrire le client: " + e.getMessage());
        }
    }

    /**
     * Récupérer tous les clients
     */
    public List<ClientDTO> getAllClients() {
        try {
            System.out.println("🔄 Récupération de la liste des clients...");
            System.out.println("🔄 Envoi de la requête vers: " + BASE_URL + "/clients");

            HttpRequest request = createRequest("/clients").GET().build();
            String response = sendRequestForString(request);

            System.out.println("📡 Réponse reçue - Status: 200");
            System.out.println("📄 Corps de la réponse: " + response);

            Type clientListType = new TypeToken<List<ClientDTO>>(){}.getType();
            List<ClientDTO> clients = gson.fromJson(response, clientListType);

            if (clients == null) {
                clients = new ArrayList<>();
            }

            System.out.println("✅ " + clients.size() + " clients récupérés avec succès");
            return clients;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de la liste des clients: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la liste des clients: " + e.getMessage());
        }
    }

    /**
     * Récupérer un client par son ID
     */
    public ClientDTO getClientById(Long id) {
        try {
            System.out.println("🔄 Récupération du client ID: " + id);

            HttpRequest request = createRequest("/clients/" + id).GET().build();
            String response = sendRequestForString(request);

            ClientDTO client = gson.fromJson(response, ClientDTO.class);
            System.out.println("✅ Client récupéré: " + client.getNomComplet());
            return client;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du client ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer le client: " + e.getMessage());
        }
    }

    /**
     * Créer un nouveau client
     */
    public ClientDTO createClient(ClientDTO client) {
        try {
            System.out.println("🔄 Création d'un nouveau client: " + client.getEmail());

            String json = gson.toJson(client);
            System.out.println("📤 Création client avec données: " + json);

            HttpRequest request = createRequest("/clients")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (response.body() == null || response.body().trim().isEmpty()) {
                    System.out.println("⚠️ Réponse vide du serveur, client probablement créé");
                    return client; // Retourner le client original si pas de réponse
                }

                ClientDTO clientCree = gson.fromJson(response.body(), ClientDTO.class);
                System.out.println("✅ Client créé avec succès: " + clientCree.getEmail());
                return clientCree;
            } else {
                System.err.println("❌ Erreur lors de la création du client - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du client: " + e.getMessage());
            throw new RuntimeException("Impossible de créer le client: " + e.getMessage());
        }
    }

    /**
     * Mettre à jour un client
     */
    public ClientDTO updateClient(ClientDTO client) {
        try {
            System.out.println("🔄 Mise à jour du client: " + client.getEmail());

            String json = gson.toJson(client);
            System.out.println("📤 Mise à jour client avec données: " + json);

            HttpRequest request = createRequest("/clients/" + client.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ClientDTO clientModifie = gson.fromJson(response.body(), ClientDTO.class);
                System.out.println("✅ Client mis à jour avec succès: " + clientModifie.getEmail());
                return clientModifie;
            } else {
                System.err.println("❌ Erreur lors de la mise à jour du client - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du client: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le client: " + e.getMessage());
        }
    }

    /**
     * Supprimer un client
     */
    public void deleteClient(Long id) {
        try {
            System.out.println("🔄 Suppression du client ID: " + id);

            HttpRequest request = createRequest("/clients/" + id)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("✅ Client supprimé avec succès");
            } else {
                System.err.println("❌ Erreur lors de la suppression du client - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du client: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer le client: " + e.getMessage());
        }
    }

    /**
     * Suspendre un client
     */
    public ClientDTO suspendClient(Long id) {
        try {
            System.out.println("🔄 Suspension du client ID: " + id);

            HttpRequest request = createRequest("/clients/" + id + "/suspend")
                    .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ClientDTO clientSuspendu = gson.fromJson(response.body(), ClientDTO.class);
                System.out.println("✅ Client suspendu avec succès: " + clientSuspendu.getEmail());
                return clientSuspendu;
            } else {
                System.err.println("❌ Erreur lors de la suspension du client - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suspension du client: " + e.getMessage());
            throw new RuntimeException("Impossible de suspendre le client: " + e.getMessage());
        }
    }

    /**
     * Réactiver un client
     */
    public ClientDTO reactivateClient(Long id) {
        try {
            System.out.println("🔄 Réactivation du client ID: " + id);

            HttpRequest request = createRequest("/clients/" + id + "/reactivate")
                    .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Réponse reçue - Status: " + response.statusCode());
            System.out.println("📄 Corps de la réponse: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                ClientDTO clientReactive = gson.fromJson(response.body(), ClientDTO.class);
                System.out.println("✅ Client réactivé avec succès: " + clientReactive.getEmail());
                return clientReactive;
            } else {
                System.err.println("❌ Erreur lors de la réactivation du client - Status: " + response.statusCode());
                throw new RuntimeException("Erreur API: " + response.statusCode() + " - " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la réactivation du client: " + e.getMessage());
            throw new RuntimeException("Impossible de réactiver le client: " + e.getMessage());
        }
    }

    /**
     * Vérifier si un client existe
     */
    public boolean clientExists(Long id) {
        try {
            getClientById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifier si un email existe déjà
     */
    public boolean emailExists(String email) {
        try {
            List<ClientDTO> clients = getAllClients();
            return clients.stream().anyMatch(client -> email.equals(client.getEmail()));
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification de l'email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Rechercher des clients par nom ou email
     */
    public List<ClientDTO> searchClients(String searchTerm) {
        try {
            List<ClientDTO> allClients = getAllClients();
            List<ClientDTO> filteredClients = new ArrayList<>();

            String lowerSearchTerm = searchTerm.toLowerCase();

            for (ClientDTO client : allClients) {
                if (client.getNom().toLowerCase().contains(lowerSearchTerm) ||
                        client.getPrenom().toLowerCase().contains(lowerSearchTerm) ||
                        client.getEmail().toLowerCase().contains(lowerSearchTerm) ||
                        client.getTelephone().contains(searchTerm)) {
                    filteredClients.add(client);
                }
            }

            System.out.println("🔍 " + filteredClients.size() + " clients trouvés pour: " + searchTerm);
            return filteredClients;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche de clients: " + e.getMessage());
            throw new RuntimeException("Impossible de rechercher les clients: " + e.getMessage());
        }
    }

    /**
     * Obtenir les statistiques des clients
     */
    public ClientStatistics getClientStatistics() {
        try {
            List<ClientDTO> clients = getAllClients();

            int total = clients.size();
            int actifs = (int) clients.stream().filter(c -> "Actif".equals(c.getStatut())).count();
            int suspendus = (int) clients.stream().filter(c -> "Suspendu".equals(c.getStatut())).count();
            int fermes = (int) clients.stream().filter(c -> "Fermé".equals(c.getStatut())).count();

            return new ClientStatistics(total, actifs, suspendus, fermes);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du calcul des statistiques: " + e.getMessage());
            return new ClientStatistics(0, 0, 0, 0);
        }
    }

    // === CLASSES INTERNES POUR LES REQUÊTES ET RÉPONSES ===

    public static class LoginRequest {
        public String email;  // ✅ CORRECTION : Champs publics pour Gson
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        // Getters et setters publics
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        public boolean success;  // ✅ CORRECTION : Champs publics pour Gson
        public String message;
        public ClientDTO client;

        // Getters et setters publics
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public ClientDTO getClient() { return client; }
        public void setClient(ClientDTO client) { this.client = client; }
    }

    public static class ClientStatistics {
        private int total;
        private int actifs;
        private int suspendus;
        private int fermes;

        public ClientStatistics(int total, int actifs, int suspendus, int fermes) {
            this.total = total;
            this.actifs = actifs;
            this.suspendus = suspendus;
            this.fermes = fermes;
        }

        // Getters
        public int getTotal() { return total; }
        public int getActifs() { return actifs; }
        public int getSuspendus() { return suspendus; }
        public int getFermes() { return fermes; }

        @Override
        public String toString() {
            return "ClientStatistics{" +
                    "total=" + total +
                    ", actifs=" + actifs +
                    ", suspendus=" + suspendus +
                    ", fermes=" + fermes +
                    '}';
        }
    }
}