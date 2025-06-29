package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.TicketSupportDTO;
import java.net.http.HttpRequest;
import java.util.List;

public class TicketSupportService extends ApiService {

    public List<TicketSupportDTO> getAllTickets() {
        try {
            HttpRequest request = createRequest("/tickets").GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TicketSupportDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des tickets: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la liste des tickets: " + e.getMessage());
        }
    }

    public TicketSupportDTO getTicketById(Long id) {
        try {
            HttpRequest request = createRequest("/tickets/" + id).GET().build();
            return sendRequest(request, TicketSupportDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du ticket: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer le ticket: " + e.getMessage());
        }
    }

    public List<TicketSupportDTO> getTicketsByClient(Long clientId) {
        try {
            HttpRequest request = createRequest("/tickets/client/" + clientId).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TicketSupportDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des tickets du client: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les tickets du client: " + e.getMessage());
        }
    }

    public List<TicketSupportDTO> getTicketsByStatut(String statut) {
        try {
            HttpRequest request = createRequest("/tickets/statut/" + statut).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TicketSupportDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des tickets par statut: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les tickets: " + e.getMessage());
        }
    }

    public TicketSupportDTO createTicket(TicketSupportDTO ticket) {
        try {
            String json = gson.toJson(ticket);
            HttpRequest request = createRequest("/tickets")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, TicketSupportDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du ticket: " + e.getMessage());
            throw new RuntimeException("Impossible de créer le ticket: " + e.getMessage());
        }
    }

    public TicketSupportDTO updateTicket(TicketSupportDTO ticket) {
        try {
            String json = gson.toJson(ticket);
            HttpRequest request = createRequest("/tickets/" + ticket.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, TicketSupportDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du ticket: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour le ticket: " + e.getMessage());
        }
    }

    public void deleteTicket(Long id) {
        try {
            HttpRequest request = createRequest("/tickets/" + id).DELETE().build();
            sendRequestForString(request);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du ticket: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer le ticket: " + e.getMessage());
        }
    }

    public void resoudreTicket(Long id, String reponse) {
        try {
            String json = gson.toJson(new ReponseTicket(reponse));
            HttpRequest request = createRequest("/tickets/" + id + "/resoudre")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Ticket résolu avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la résolution du ticket: " + e.getMessage());
            throw new RuntimeException("Impossible de résoudre le ticket: " + e.getMessage());
        }
    }

    // Classe interne pour la réponse
    private static class ReponseTicket {
        private final String reponse;

        public ReponseTicket(String reponse) {
            this.reponse = reponse;
        }

        public String getReponse() {
            return reponse;
        }
    }
}