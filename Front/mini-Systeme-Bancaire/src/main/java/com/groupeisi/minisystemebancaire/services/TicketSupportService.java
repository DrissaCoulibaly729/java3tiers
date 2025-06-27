package com.groupeisi.minisystemebancaire.services;

import com.google.gson.GsonBuilder;
import com.groupeisi.minisystemebancaire.dto.TicketSupportDTO;
import com.google.gson.Gson;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TicketSupportService {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = Converters.registerAll(new GsonBuilder()).create();
    private final String BASE_URL = "http://localhost:8000/api/ticket-supports";

    // ✅ Soumettre une réclamation (Créer un ticket)
    public void soumettreTicket(TicketSupportDTO ticketDTO) {
        try {
            ticketDTO.setStatut("Ouvert");
            String json = gson.toJson(ticketDTO);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new RuntimeException("Erreur création ticket : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API soumettreTicket", e);
        }
    }

    // ✅ Lire : Récupérer un ticket par ID
    public TicketSupportDTO getTicketById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), TicketSupportDTO.class);
            }
            throw new RuntimeException("Ticket non trouvé !");
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getTicketById", e);
        }
    }

    // ✅ Lire : Récupérer tous les tickets d'un client
    public List<TicketSupportDTO> getTicketsByClient(Long clientId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/api/clients/" + clientId + "/tickets"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return gson.fromJson(response.body(),
                    new TypeToken<List<TicketSupportDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getTicketsByClient", e);
        }
    }

    // ✅ Lire : Récupérer tous les tickets
    public List<TicketSupportDTO> getAllTickets() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return gson.fromJson(response.body(),
                    new TypeToken<List<TicketSupportDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API getAllTickets", e);
        }
    }

    // ✅ Rechercher un ticket par ID ou Sujet
    public List<TicketSupportDTO> rechercherTicket(String recherche) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/recherche/" + recherche))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return gson.fromJson(response.body(),
                    new TypeToken<List<TicketSupportDTO>>() {}.getType());
        } catch (Exception e) {
            throw new RuntimeException("Erreur API rechercherTicket", e);
        }
    }

    // ✅ Répondre à un ticket
    public void repondreTicket(Long ticketId, String reponse) {
        try {
            String json = "{\"reponse\":\"" + reponse + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + ticketId + "/repondre"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur réponse ticket : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API repondreTicket", e);
        }
    }

    // ✅ Marquer un ticket comme résolu
    public void marquerTicketResolu(Long ticketId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + ticketId + "/resolu"))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur marquage résolu : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API marquerTicketResolu", e);
        }
    }

    // ✅ Générer un rapport (Simulé)
    public void genererRapport(String type, String periode) {
        System.out.println("📊 Rapport généré : " + type + " pour " + periode);
    }

    // ✅ Supprimer un ticket (Si résolu)
    public void deleteTicket(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + id))
                    .DELETE()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 204) {
                throw new RuntimeException("Erreur suppression ticket : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API deleteTicket", e);
        }
    }

    // ✅ Mettre à jour un ticket
    public void updateTicket(TicketSupportDTO ticketDTO) {
        try {
            String json = gson.toJson(ticketDTO);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/" + ticketDTO.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur mise à jour ticket : " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur API updateTicket", e);
        }
    }
}
