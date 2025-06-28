package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.CreditDTO;

import java.net.http.HttpRequest;
import java.util.List;

public class CreditService extends ApiService {

    public List<CreditDTO> getAllCredits() {
        HttpRequest request = createRequest("/credits").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<CreditDTO>>(){}.getType());
    }

    public CreditDTO getCreditById(Long id) {
        HttpRequest request = createRequest("/credits/" + id).GET().build();
        return sendRequest(request, CreditDTO.class);
    }

    public List<CreditDTO> getCreditsByClient(Long clientId) {
        HttpRequest request = createRequest("/credits/client/" + clientId).GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<CreditDTO>>(){}.getType());
    }

    public List<CreditDTO> getCreditsByStatut(String statut) {
        HttpRequest request = createRequest("/credits/statut/" + statut).GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<CreditDTO>>(){}.getType());
    }

    public CreditDTO createCredit(CreditDTO credit) {
        String json = gson.toJson(credit);
        HttpRequest request = createRequest("/credits")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, CreditDTO.class);
    }

    public CreditDTO updateCredit(CreditDTO credit) {
        String json = gson.toJson(credit);
        HttpRequest request = createRequest("/credits/" + credit.getId())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, CreditDTO.class);
    }

    public void deleteCredit(Long id) {
        HttpRequest request = createRequest("/credits/" + id).DELETE().build();
        sendRequestForString(request);
    }

    public void accepterCredit(Long id) {
        HttpRequest request = createRequest("/credits/" + id + "/accepter")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRequestForString(request);
    }

    public void refuserCredit(Long id) {
        HttpRequest request = createRequest("/credits/" + id + "/refuser")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRequestForString(request);
    }
}