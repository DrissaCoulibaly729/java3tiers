package com.groupeisi.minisystemebancaire.services;

import com.groupeisi.minisystemebancaire.dtos.CreditDTo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CreditService {

    /**
     * Demander un crédit
     */
    public static CompletableFuture<CreditDTo> demanderCredit(Long clientId, BigDecimal montant, Integer dureeEnMois, String motif) {
        Map<String, Object> request = new HashMap<>();
        request.put("client_id", clientId);
        request.put("montant", montant);
        request.put("duree_en_mois", dureeEnMois);
        request.put("motif", motif);

        return HttpService.postAsync("/api/credits", request, CreditDTo.class);
    }

    /**
     * Obtenir les crédits d'un client
     */
    public static CompletableFuture<List<CreditDTo>> getCreditsByClient(Long clientId) {
        return HttpService.getListAsync("/api/credits/client/" + clientId, CreditDTo.class);
    }

    /**
     * Obtenir tous les crédits (pour admin)
     */
    public static CompletableFuture<List<CreditDTo>> getAllCredits() {
        return HttpService.getListAsync("/api/credits", CreditDTo.class);
    }

    /**
     * Approuver/Refuser un crédit (admin)
     */
    public static CompletableFuture<CreditDTo> updateCreditStatus(Long creditId, String statut, String commentaire) {
        Map<String, String> request = new HashMap<>();
        request.put("statut", statut);
        request.put("commentaire", commentaire);

        return HttpService.putAsync("/api/credits/" + creditId + "/status", request, CreditDTo.class);
    }

    /**
     * Simuler un crédit - Version corrigée
     */
    public static CompletableFuture<SimulationCreditResponse> simulerCredit(BigDecimal montant, Integer dureeEnMois, BigDecimal tauxInteret) {
        Map<String, Object> request = new HashMap<>();
        request.put("montant", montant);
        request.put("duree_en_mois", dureeEnMois);
        request.put("taux_interet", tauxInteret);

        return HttpService.postAsync("/api/credits/simulation", request, SimulationCreditResponse.class);
    }

    /**
     * Version alternative qui retourne Map<String, Object>
     */
    public static CompletableFuture<Map<String, Object>> simulerCreditMap(BigDecimal montant, Integer dureeEnMois, BigDecimal tauxInteret) {
        Map<String, Object> request = new HashMap<>();
        request.put("montant", montant);
        request.put("duree_en_mois", dureeEnMois);
        request.put("taux_interet", tauxInteret);

        // Utiliser une approche différente pour éviter le problème de types
        return HttpService.postAsync("/api/credits/simulation", request, Object.class)
                .thenApply(response -> {
                    if (response instanceof Map) {
                        return (Map<String, Object>) response;
                    } else {
                        // Fallback en cas de réponse inattendue
                        Map<String, Object> result = new HashMap<>();
                        result.put("response", response);
                        return result;
                    }
                });
    }

    /**
     * Classe pour la réponse de simulation
     */
    public static class SimulationCreditResponse {
        private Double mensualite;
        private Double coutTotal;
        private Double taux;
        private Integer duree;
        private BigDecimal montant;

        // Constructeurs
        public SimulationCreditResponse() {}

        // Getters et setters
        public Double getMensualite() { return mensualite; }
        public void setMensualite(Double mensualite) { this.mensualite = mensualite; }

        public Double getCoutTotal() { return coutTotal; }
        public void setCoutTotal(Double coutTotal) { this.coutTotal = coutTotal; }

        public Double getTaux() { return taux; }
        public void setTaux(Double taux) { this.taux = taux; }

        public Integer getDuree() { return duree; }
        public void setDuree(Integer duree) { this.duree = duree; }

        public BigDecimal getMontant() { return montant; }
        public void setMontant(BigDecimal montant) { this.montant = montant; }
    }
}