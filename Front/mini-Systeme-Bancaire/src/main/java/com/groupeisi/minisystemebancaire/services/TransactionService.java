package com.groupeisi.minisystemebancaire.services;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.util.LocalDateTimeTypeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson;

    {
        // Adaptateur perso pour le format « yyyy-MM-dd HH:mm:ss »
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        new LocalDateTimeTypeAdapter())
                .create();
    }

    private final String BASE_URL = "http://localhost:8000/api/transactions";
    private final CompteService compteService = new CompteService();

    // ✅ Créer une transaction
    public void enregistrerTransaction(TransactionDTO transactionDTO) {
        try {
            String json = gson.toJson(transactionDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL)) // ex: http://localhost:8000/api/transactions
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("➤ Code HTTP : " + response.statusCode());
            System.out.println("➤ Réponse brute : " + response.body());

            if (response.statusCode() != 201 && response.statusCode() != 200) {
                throw new RuntimeException("Erreur création transaction : " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace(); // pour voir la trace complète dans la console
            throw new RuntimeException("Erreur API enregistrerTransaction", e);
        }
    }


    // ✅ Lire : Récupérer une transaction par ID
    public TransactionDTO getTransactionById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), TransactionDTO.class);
            }
            throw new RuntimeException("Transaction non trouvée !");
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getTransactionById", e);
        }
    }

    // ✅ Lire : Récupérer toutes les transactions d'un compte
    public List<TransactionDTO> getTransactionsByCompte(Long compteId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/comptes/" + compteId + "/transactions"))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<TransactionDTO>>(){}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getTransactionsByCompte", e);
        }
    }

    // ✅ Annuler une transaction suspecte
    public void annulerTransaction(Long id) {
        try {
            String json = "{\"statut\":\"Annulée\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id + "/annuler"))
                    .header("Accept", "application/json") 
.header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur annulation : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API annulerTransaction", e);
        }
    }

    // ✅ Supprimer une transaction
    public void deleteTransaction(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 204 && response.statusCode() != 200) {
                throw new RuntimeException("Erreur suppression transaction : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API deleteTransaction", e);
        }
    }

    // ✅ Récupérer toutes les transactions
    public List<TransactionDTO> getAllTransactions() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<TransactionDTO>>(){}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getAllTransactions", e);
        }
    }

    // ✅ Récupérer les transactions suspectes
    public List<TransactionDTO> getTransactionsSuspectes() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/suspectes")) // ou ton URL exacte
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String json = response.body().trim();
            System.out.println("Réponse brute (transactions suspectes) : " + json);

            if (response.statusCode() != 200) {
                throw new RuntimeException("API getTransactionsSuspectes → " + response.statusCode() + " : " + json);
            }

            JsonElement root = JsonParser.parseString(json);

            if (root.isJsonArray()) {
                return gson.fromJson(root, new TypeToken<List<TransactionDTO>>() {}.getType());
            }

            // gestion d’un message texte (erreur ou info)
            if (root.isJsonPrimitive() && root.getAsJsonPrimitive().isString()) {
                System.out.println("Message de l’API : " + root.getAsString());
                return Collections.emptyList(); // ou throw si tu préfères l'erreur
            }

            throw new RuntimeException("Format inattendu : " + json);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erreur réseau getTransactionsSuspectes", e);
        }
    }


    // ✅ Mettre à jour une transaction existante
    public void updateTransaction(TransactionDTO transactionDTO) {
        try {
            String json = gson.toJson(transactionDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + transactionDTO.getId()))
                    .header("Accept", "application/json") 
.header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur mise à jour transaction : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API updateTransaction", e);
        }
    }

    public List<TransactionDTO> getTransactionsByClientId(Long clientId) {
        List<CompteDTO> comptes = compteService.getComptesByClientId(clientId);
        List<TransactionDTO> transactions = new ArrayList<>();
        for (CompteDTO compte : comptes) {
            transactions.addAll(getTransactionsByCompte(compte.getId()));
        }
        return transactions.stream().distinct().toList();
    }
}
