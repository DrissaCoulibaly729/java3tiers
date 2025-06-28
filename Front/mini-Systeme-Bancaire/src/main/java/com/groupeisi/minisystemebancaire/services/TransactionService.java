package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;

import java.net.http.HttpRequest;
import java.util.List;

public class TransactionService extends ApiService {

    public List<TransactionDTO> getAllTransactions() {
        try {
            HttpRequest request = createRequest("/transactions").GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des transactions: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la liste des transactions: " + e.getMessage());
        }
    }

    public TransactionDTO getTransactionById(Long id) {
        try {
            HttpRequest request = createRequest("/transactions/" + id).GET().build();
            return sendRequest(request, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de la transaction ID " + id + ": " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer la transaction: " + e.getMessage());
        }
    }

    public List<TransactionDTO> getTransactionsByCompte(Long compteId) {
        try {
            HttpRequest request = createRequest("/transactions/compte/" + compteId).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des transactions du compte: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les transactions du compte: " + e.getMessage());
        }
    }

    public List<TransactionDTO> getTransactionsByClient(Long clientId) {
        try {
            HttpRequest request = createRequest("/transactions/client/" + clientId).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des transactions du client: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les transactions du client: " + e.getMessage());
        }
    }

    public List<TransactionDTO> getTransactionsByStatut(String statut) {
        try {
            HttpRequest request = createRequest("/transactions/statut/" + statut).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des transactions par statut: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les transactions: " + e.getMessage());
        }
    }

    public TransactionDTO createTransaction(TransactionDTO transaction) {
        try {
            String json = gson.toJson(transaction);
            System.out.println("📤 Création transaction avec données: " + json);

            HttpRequest request = createRequest("/transactions")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de la transaction: " + e.getMessage());
            throw new RuntimeException("Impossible de créer la transaction: " + e.getMessage());
        }
    }

    public TransactionDTO updateTransaction(TransactionDTO transaction) {
        try {
            String json = gson.toJson(transaction);
            HttpRequest request = createRequest("/transactions/" + transaction.getId())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de la transaction: " + e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour la transaction: " + e.getMessage());
        }
    }

    public void deleteTransaction(Long id) {
        try {
            HttpRequest request = createRequest("/transactions/" + id).DELETE().build();
            sendRequestForString(request);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression de la transaction: " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer la transaction: " + e.getMessage());
        }
    }

    public void validerTransaction(Long id) {
        try {
            HttpRequest request = createRequest("/transactions/" + id + "/valider")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Transaction validée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la validation de la transaction: " + e.getMessage());
            throw new RuntimeException("Impossible de valider la transaction: " + e.getMessage());
        }
    }

    public void rejeterTransaction(Long id, String motif) {
        try {
            String json = gson.toJson(new MotifRejet(motif));
            HttpRequest request = createRequest("/transactions/" + id + "/rejeter")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Transaction rejetée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du rejet de la transaction: " + e.getMessage());
            throw new RuntimeException("Impossible de rejeter la transaction: " + e.getMessage());
        }
    }

    public TransactionDTO effectuerDepot(Long compteId, double montant, String description) {
        try {
            DepotRequest request = new DepotRequest(compteId, montant, description);
            String json = gson.toJson(request);

            HttpRequest httpRequest = createRequest("/transactions/depot")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(httpRequest, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du dépôt: " + e.getMessage());
            throw new RuntimeException("Impossible d'effectuer le dépôt: " + e.getMessage());
        }
    }

    public TransactionDTO effectuerRetrait(Long compteId, double montant, String description) {
        try {
            RetraitRequest request = new RetraitRequest(compteId, montant, description);
            String json = gson.toJson(request);

            HttpRequest httpRequest = createRequest("/transactions/retrait")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(httpRequest, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du retrait: " + e.getMessage());
            throw new RuntimeException("Impossible d'effectuer le retrait: " + e.getMessage());
        }
    }

    public TransactionDTO effectuerVirement(Long compteSourceId, Long compteDestId,
                                            double montant, String description) {
        try {
            VirementRequest request = new VirementRequest(compteSourceId, compteDestId, montant, description);
            String json = gson.toJson(request);

            HttpRequest httpRequest = createRequest("/transactions/virement")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(httpRequest, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du virement: " + e.getMessage());
            throw new RuntimeException("Impossible d'effectuer le virement: " + e.getMessage());
        }
    }

    public void annulerTransaction(Long id) {
        try {
            HttpRequest request = createRequest("/transactions/" + id + "/annuler")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Transaction annulée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'annulation de la transaction: " + e.getMessage());
            throw new RuntimeException("Impossible d'annuler la transaction: " + e.getMessage());
        }
    }

    public List<TransactionDTO> getTransactionsSuspectes() {
        try {
            HttpRequest request = createRequest("/transactions/suspectes").GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des transactions suspectes: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les transactions suspectes: " + e.getMessage());
        }
    }

    public List<TransactionDTO> getTransactionsRecentes(int limit) {
        try {
            HttpRequest request = createRequest("/transactions/recentes?limit=" + limit).GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des transactions récentes: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les transactions récentes: " + e.getMessage());
        }
    }

    public List<TransactionDTO> getTransactionsByDateRange(String dateDebut, String dateFin) {
        try {
            HttpRequest request = createRequest("/transactions/periode?debut=" + dateDebut + "&fin=" + dateFin)
                    .GET().build();
            String response = sendRequestForString(request);
            return gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des transactions par période: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les transactions: " + e.getMessage());
        }
    }

    // Classes internes pour les requêtes spécifiques
    public static class MotifRejet {
        private final String motif;

        public MotifRejet(String motif) {
            this.motif = motif;
        }

        public String getMotif() { return motif; }
    }

    public static class DepotRequest {
        private final Long compteId;
        private final double montant;
        private final String description;

        public DepotRequest(Long compteId, double montant, String description) {
            this.compteId = compteId;
            this.montant = montant;
            this.description = description;
        }

        public Long getCompteId() { return compteId; }
        public double getMontant() { return montant; }
        public String getDescription() { return description; }
    }

    public static class RetraitRequest {
        private final Long compteId;
        private final double montant;
        private final String description;

        public RetraitRequest(Long compteId, double montant, String description) {
            this.compteId = compteId;
            this.montant = montant;
            this.description = description;
        }

        public Long getCompteId() { return compteId; }
        public double getMontant() { return montant; }
        public String getDescription() { return description; }
    }

    public static class VirementRequest {
        private final Long compteSourceId;
        private final Long compteDestId;
        private final double montant;
        private final String description;

        public VirementRequest(Long compteSourceId, Long compteDestId, double montant, String description) {
            this.compteSourceId = compteSourceId;
            this.compteDestId = compteDestId;
            this.montant = montant;
            this.description = description;
        }

        public Long getCompteSourceId() { return compteSourceId; }
        public Long getCompteDestId() { return compteDestId; }
        public double getMontant() { return montant; }
        public String getDescription() { return description; }
    }
}