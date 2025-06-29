package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ Service pour la gestion des clients - Version corrigée avec gestion de dates
 */
public class ClientService extends ApiService {

    /**
     * Récupérer tous les clients
     */
    public List<ClientDTO> getAllClients() {
        try {
            System.out.println("🔄 Récupération de la liste des clients...");

            HttpRequest request = createRequest("/clients").GET().build();
            String response = sendRequestForString(request);

            // ✅ CORRECTION: Utiliser le nouveau deserializer robuste
            try {
                List<ClientDTO> clients = gson.fromJson(response, new TypeToken<List<ClientDTO>>(){}.getType());
                System.out.println("✅ " + clients.size() + " clients récupérés avec succès");
                return clients;
            } catch (Exception parseException) {
                System.err.println("❌ Erreur de parsing JSON clients: " + parseException.getMessage());
                System.err.println("📄 JSON reçu: " + response);

                // ✅ FALLBACK: Retourner une liste vide plutôt que de planter
                System.err.println("⚠️ Retour d'une liste vide pour éviter le crash");
                return new ArrayList<>();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des clients: " + e.getMessage());
            e.printStackTrace();
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
                    System.err.println("❌ Réponse vide du serveur");
                    return null;
                }

                try {
                    // ✅ CORRECTION: Parser la réponse complète qui contient {client: ..., compte: ...}
                    var responseObj = gson.fromJson(response.body(), com.google.gson.JsonObject.class);

                    if (responseObj.has("client")) {
                        ClientDTO savedClient = gson.fromJson(responseObj.get("client"), ClientDTO.class);
                        System.out.println("✅ Client créé avec succès: " + savedClient.getEmail());
                        return savedClient;
                    } else {
                        // Fallback: essayer de parser directement comme ClientDTO
                        ClientDTO savedClient = gson.fromJson(response.body(), ClientDTO.class);
                        System.out.println("✅ Client créé avec succès: " + savedClient.getEmail());
                        return savedClient;
                    }

                } catch (Exception parseException) {
                    System.err.println("❌ Erreur de parsing JSON création client: " + parseException.getMessage());
                    System.err.println("📄 JSON à parser: " + response.body());
                    throw new RuntimeException("Erreur de parsing de la réponse");
                }
            } else {
                String errorMessage = "Erreur lors de la création du client - Status: " + response.statusCode();
                if (response.body() != null && !response.body().isEmpty()) {
                    if (response.statusCode() == 422) {
                        errorMessage = "Données invalides. Vérifiez tous les champs obligatoires.";
                    } else if (response.statusCode() == 500) {
                        errorMessage = "Erreur serveur interne";
                    } else {
                        errorMessage += " - " + response.body();
                    }
                }
                System.err.println("❌ " + errorMessage);
                throw new RuntimeException(errorMessage);
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

    /**
     * Mettre à jour un client
     */
    public ClientDTO updateClient(ClientDTO client) {
        try {
            System.out.println("🔄 Mise à jour du client: " + client.getEmail());

            String json = gson.toJson(client);
            HttpRequest request = createRequest("/clients/" + client.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            String response = sendRequestForString(request);

            var responseObj = gson.fromJson(response, com.google.gson.JsonObject.class);
            ClientDTO updatedClient;

            if (responseObj.has("client")) {
                updatedClient = gson.fromJson(responseObj.get("client"), ClientDTO.class);
            } else {
                updatedClient = gson.fromJson(response, ClientDTO.class);
            }

            System.out.println("✅ Client mis à jour: " + updatedClient.getEmail());
            return updatedClient;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du client: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le client: " + e.getMessage(), e);
        }
    }

    /**
     * Supprimer un client
     */
    public void deleteClient(Long id) {
        try {
            System.out.println("🔄 Suppression du client ID: " + id);

            HttpRequest request = createRequest("/clients/" + id).DELETE().build();
            sendRequestForString(request);

            System.out.println("✅ Client supprimé avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du client: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer le client: " + e.getMessage());
        }
    }

    /**
     * Connexion d'un client
     */
    public ClientDTO login(String email, String password) {
        try {
            System.out.println("🔐 Tentative de connexion client...");

            var loginData = new java.util.HashMap<String, String>();
            loginData.put("email", email);
            loginData.put("password", password);

            String json = gson.toJson(loginData);
            System.out.println("📤 JSON envoyé: " + json);

            HttpRequest request = createRequest("/clients/login")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Status de connexion client: " + response.statusCode());
            System.out.println("📄 Réponse brute de connexion client: " + response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (response.body() == null || response.body().trim().isEmpty()) {
                    System.err.println("❌ Réponse vide du serveur");
                    return null;
                }

                try {
                    ClientDTO client = gson.fromJson(response.body(), ClientDTO.class);
                    System.out.println("✅ Connexion client réussie pour: " + client.getEmail());
                    return client;
                } catch (Exception parseException) {
                    System.err.println("❌ Erreur de parsing JSON: " + parseException.getMessage());
                    System.err.println("📄 JSON à parser: " + response.body());
                    throw new RuntimeException("Erreur de parsing de la réponse");
                }
            } else if (response.statusCode() == 401) {
                System.err.println("❌ Identifiants incorrects (401)");
                throw new RuntimeException("Identifiants incorrects");
            } else if (response.statusCode() == 403) {
                System.err.println("❌ Compte suspendu (403)");
                throw new RuntimeException("Compte suspendu");
            } else {
                System.err.println("❌ Erreur serveur: " + response.statusCode() + " - " + response.body());
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

    /**
     * Inscription d'un nouveau client
     */
    public ClientDTO registerClient(ClientDTO client) {
        try {
            System.out.println("📝 Inscription d'un nouveau client: " + client.getEmail());

            String json = gson.toJson(client);
            HttpRequest request = createRequest("/clients/register")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            String response = sendRequestForString(request);

            var responseObj = gson.fromJson(response, com.google.gson.JsonObject.class);
            ClientDTO registeredClient;

            if (responseObj.has("client")) {
                registeredClient = gson.fromJson(responseObj.get("client"), ClientDTO.class);
            } else {
                registeredClient = gson.fromJson(response, ClientDTO.class);
            }

            System.out.println("✅ Client inscrit avec succès: " + registeredClient.getEmail());
            return registeredClient;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'inscription: " + e.getMessage());
            throw new RuntimeException("Impossible d'inscrire le client: " + e.getMessage(), e);
        }
    }

    /**
     * Suspendre un client
     */
    public void suspendClient(Long id) {
        try {
            System.out.println("🔄 Suspension du client ID: " + id);

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

    /**
     * Réactiver un client
     */
    public void reactivateClient(Long id) {
        try {
            System.out.println("🔄 Réactivation du client ID: " + id);

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

}