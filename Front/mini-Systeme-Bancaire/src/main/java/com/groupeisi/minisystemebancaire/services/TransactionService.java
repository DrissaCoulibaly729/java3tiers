package com.groupeisi.minisystemebancaire.services;

import com.google.gson.reflect.TypeToken;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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

    // ✅ AJOUT: Méthodes pour effectuer les transactions
    public TransactionDTO effectuerDepot(Long compteId, double montant, String description) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "Dépôt");
            data.put("montant", montant);
            data.put("compte_dest_id", compteId);
            data.put("description", description != null ? description : "Dépôt en espèces");
            data.put("statut", "Validé");

            String json = gson.toJson(data);
            System.out.println("📤 Création dépôt avec données: " + json);

            HttpRequest request = createRequest("/transactions")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du dépôt: " + e.getMessage());
            throw new RuntimeException("Impossible d'effectuer le dépôt: " + e.getMessage());
        }
    }

    public TransactionDTO effectuerRetrait(Long compteId, double montant, String description) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "Retrait");
            data.put("montant", montant);
            data.put("compte_source_id", compteId);
            data.put("description", description != null ? description : "Retrait en espèces");
            data.put("statut", "Validé");

            String json = gson.toJson(data);
            System.out.println("📤 Création retrait avec données: " + json);

            HttpRequest request = createRequest("/transactions")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du retrait: " + e.getMessage());
            throw new RuntimeException("Impossible d'effectuer le retrait: " + e.getMessage());
        }
    }

    public TransactionDTO effectuerVirement(Long compteSourceId, Long compteDestId, double montant, String description) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", "Virement");
            data.put("montant", montant);
            data.put("compte_source_id", compteSourceId);
            data.put("compte_dest_id", compteDestId);
            data.put("description", description != null ? description : "Virement bancaire");
            data.put("statut", "Validé");

            String json = gson.toJson(data);
            System.out.println("📤 Création virement avec données: " + json);

            HttpRequest request = createRequest("/transactions")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            return sendRequest(request, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du virement: " + e.getMessage());
            throw new RuntimeException("Impossible d'effectuer le virement: " + e.getMessage());
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
            Map<String, String> data = new HashMap<>();
            data.put("statut", "Validé");
            String json = gson.toJson(data);

            HttpRequest request = createRequest("/transactions/" + id)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
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
            Map<String, String> data = new HashMap<>();
            data.put("statut", "Rejeté");
            data.put("description", motif);
            String json = gson.toJson(data);

            HttpRequest request = createRequest("/transactions/" + id)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            sendRequestForString(request);
            System.out.println("✅ Transaction rejetée avec succès");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du rejet de la transaction: " + e.getMessage());
            throw new RuntimeException("Impossible de rejeter la transaction: " + e.getMessage());
        }
    }

    // ✅ AJOUT: Méthodes utilitaires manquantes
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

    // ✅ AJOUT: Méthodes manquantes utilisées dans votre code

    /**
     * Méthode getTransactionsRecentes() utilisée dans AdminDashboardController
     */
    public List<TransactionDTO> getTransactionsRecentes(int limit) {
        try {
            HttpRequest request = createRequest("/transactions?limit=" + limit).GET().build();
            String response = sendRequestForString(request);
            List<TransactionDTO> allTransactions = gson.fromJson(response, new TypeToken<List<TransactionDTO>>(){}.getType());

            // Retourner seulement les 'limit' premières transactions
            return allTransactions.stream()
                    .limit(limit)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des transactions récentes: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les transactions récentes: " + e.getMessage());
        }
    }

    /**
     * Méthode annulerTransaction() utilisée dans AdminTransactionsController
     */
    public TransactionDTO annulerTransaction(Long id) {
        try {
            HttpRequest request = createRequest("/transactions/" + id + "/annuler")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();
            return sendRequest(request, TransactionDTO.class);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'annulation de la transaction: " + e.getMessage());
            throw new RuntimeException("Impossible d'annuler la transaction: " + e.getMessage());
        }
    }

    // Classes internes pour les requêtes spécifiques (gardez les existantes)
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