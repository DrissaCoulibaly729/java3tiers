package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.TicketSupportDTO;

import java.net.http.HttpRequest;
import java.util.List;

public class TicketSupportService extends ApiService {

    public List<TicketSupportDTO> getAllTickets() {
        HttpRequest request = createRequest("/ticket-supports").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<TicketSupportDTO>>(){}.getType());
    }

    public TicketSupportDTO getTicketById(Long id) {
        HttpRequest request = createRequest("/ticket-supports/" + id).GET().build();
        return sendRequest(request, TicketSupportDTO.class);
    }

    public List<TicketSupportDTO> getTicketsByClient(Long clientId) {
        HttpRequest request = createRequest("/tickets/client/" + clientId).GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<TicketSupportDTO>>(){}.getType());
    }

    public TicketSupportDTO createTicket(TicketSupportDTO ticket) {
        String json = gson.toJson(ticket);
        HttpRequest request = createRequest("/ticket-supports")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, TicketSupportDTO.class);
    }

    public TicketSupportDTO updateTicket(TicketSupportDTO ticket) {
        String json = gson.toJson(ticket);
        HttpRequest request = createRequest("/ticket-supports/" + ticket.getId())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, TicketSupportDTO.class);
    }

    public void deleteTicket(Long id) {
        HttpRequest request = createRequest("/ticket-supports/" + id).DELETE().build();
        sendRequestForString(request);
    }

    public void repondreTicket(Long id, String reponse) {
        String json = gson.toJson(new ReponseRequest(reponse));
        HttpRequest request = createRequest("/ticket-supports/" + id + "/repondre")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        sendRequestForString(request);
    }

    public void marquerResolu(Long id) {
        HttpRequest request = createRequest("/ticket-supports/" + id + "/resolu")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRequestForString(request);
    }

    private static class ReponseRequest {
        private final String reponse;

        public ReponseRequest(String reponse) {
            this.reponse = reponse;
        }
    }
}
