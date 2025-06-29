package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class CreditDTO {
    private Long id;

    @SerializedName("client_id")
    private Long clientId;

    private Double montant;
    private Integer duree; // en mois

    @SerializedName("taux_interet")
    private Double tauxInteret;

    private String statut; // "En attente", "Approuvé", "Rejeté", "Remboursé"

    @SerializedName("date_demande")
    private String dateDemande;

    @SerializedName("date_approbation")
    private String dateApprobation;

    @SerializedName("montant_mensuel")
    private Double montantMensuel;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Référence au client
    private ClientDTO client;

    // Constructeurs
    public CreditDTO() {}

    public CreditDTO(Long clientId, Double montant, Integer duree, Double tauxInteret) {
        this.clientId = clientId;
        this.montant = montant;
        this.duree = duree;
        this.tauxInteret = tauxInteret;
        this.statut = "En attente";
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public Integer getDuree() { return duree; }
    public void setDuree(Integer duree) { this.duree = duree; }

    public Double getTauxInteret() { return tauxInteret; }
    public void setTauxInteret(Double tauxInteret) { this.tauxInteret = tauxInteret; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDateDemande() { return dateDemande; }
    public void setDateDemande(String dateDemande) { this.dateDemande = dateDemande; }

    public String getDateApprobation() { return dateApprobation; }
    public void setDateApprobation(String dateApprobation) { this.dateApprobation = dateApprobation; }

    public Double getMontantMensuel() { return montantMensuel; }
    public void setMontantMensuel(Double montantMensuel) { this.montantMensuel = montantMensuel; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public ClientDTO getClient() { return client; }
    public void setClient(ClientDTO client) { this.client = client; }

    // Méthodes utilitaires
    public String getClientNom() {
        return client != null ? client.getNomComplet() : "Client inconnu";
    }

    public boolean isApprouve() {
        return "Approuvé".equals(statut);
    }

    public boolean isEnAttente() {
        return "En attente".equals(statut);
    }

    public boolean isRejete() {
        return "Rejeté".equals(statut);
    }

    @Override
    public String toString() {
        return "CreditDTO{" +
                "id=" + id +
                ", montant=" + montant +
                ", statut='" + statut + '\'' +
                '}';
    }
}