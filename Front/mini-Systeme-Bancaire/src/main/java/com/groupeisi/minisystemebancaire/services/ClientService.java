package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;

import java.net.http.HttpRequest;
import java.util.List;

public class ClientService extends ApiService {

    public List<ClientDTO> getAllClients() {
        HttpRequest request = createRequest("/clients").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<ClientDTO>>(){}.getType());
    }

    public ClientDTO getClientById(Long id) {
        HttpRequest request = createRequest("/clients/" + id).GET().build();
        return sendRequest(request, ClientDTO.class);
    }

    public ClientDTO createClient(ClientDTO client) {
        String json = gson.toJson(client);
        HttpRequest request = createRequest("/clients")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, ClientDTO.class);
    }

    public ClientDTO updateClient(ClientDTO client) {
        String json = gson.toJson(client);
        HttpRequest request = createRequest("/clients/" + client.getId())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, ClientDTO.class);
    }

    public void deleteClient(Long id) {
        HttpRequest request = createRequest("/clients/" + id).DELETE().build();
        sendRequestForString(request);
    }

    public ClientDTO login(String email, String password) {
        String json = gson.toJson(new LoginRequest(email, password));
        HttpRequest request = createRequest("/clients/login")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, ClientDTO.class);
    }

    public void suspendClient(Long id) {
        HttpRequest request = createRequest("/clients/" + id + "/suspend")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRequestForString(request);
    }

    public void reactivateClient(Long id) {
        HttpRequest request = createRequest("/clients/" + id + "/reactivate")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRequestForString(request);
    }

    private static class LoginRequest {
        private final String email;
        private final String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
