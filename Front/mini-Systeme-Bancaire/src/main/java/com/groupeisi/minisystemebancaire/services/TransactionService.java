package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;

import java.net.http.HttpRequest;
import java.util.List;

public class TransactionService extends ApiService {

    public List<TransactionDTO> getAllTransactions() {
        HttpRequest request = createRequest("/transactions").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
    }

    public TransactionDTO getTransactionById(Long id) {
        HttpRequest request = createRequest("/transactions/" + id).GET().build();
        return sendRequest(request, TransactionDTO.class);
    }

    public TransactionDTO createTransaction(TransactionDTO transaction) {
        String json = gson.toJson(transaction);
        HttpRequest request = createRequest("/transactions")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, TransactionDTO.class);
    }

    public List<TransactionDTO> getTransactionsByCompte(Long compteId) {
        HttpRequest request = createRequest("/comptes/" + compteId + "/transactions").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
    }

    public List<TransactionDTO> getTransactionsByClient(Long clientId) {
        HttpRequest request = createRequest("/clients/" + clientId + "/transactions").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
    }

    public List<TransactionDTO> getTransactionsSuspectes() {
        HttpRequest request = createRequest("/transactions/suspectes").GET().build();
        String response = sendRequestForString(request);
        return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
    }

    public void annulerTransaction(Long id) {
        HttpRequest request = createRequest("/transactions/" + id + "/annuler")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        sendRequestForString(request);
    }

    public TransactionDTO updateTransaction(TransactionDTO transaction) {
        String json = gson.toJson(transaction);
        HttpRequest request = createRequest("/transactions/" + transaction.getId())
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return sendRequest(request, TransactionDTO.class);
    }
}