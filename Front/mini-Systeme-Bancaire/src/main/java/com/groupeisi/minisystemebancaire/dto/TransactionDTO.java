package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

/**
 * ✅ DTO pour la classe Transaction
 */
public class TransactionDTO {
    private Long id;
    private String type; // "Dépôt", "Retrait", "Virement"
    private Double montant;
    private LocalDateTime date;
    private String statut; // "Validé", "Rejeté", "En attente"
    private String description;

    @SerializedName("compte_source_id")
    private Long compteSourceId;

    @SerializedName("compte_dest_id")
    private Long compteDestId;

    // Références aux comptes (pour les jointures)
    private CompteDTO compteSource;
    private CompteDTO compteDestination;

    // Constructeurs
    public TransactionDTO() {}

    public TransactionDTO(String type, Double montant, Long compteSourceId) {
        this.type = type;
        this.montant = montant;
        this.compteSourceId = compteSourceId;
        this.date = LocalDateTime.now();
        this.statut = "En attente";
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getMontant() {
        return montant != null ? montant : 0.0;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCompteSourceId() {
        return compteSourceId;
    }

    public void setCompteSourceId(Long compteSourceId) {
        this.compteSourceId = compteSourceId;
    }

    public Long getCompteDestId() {
        return compteDestId;
    }

    public void setCompteDestId(Long compteDestId) {
        this.compteDestId = compteDestId;
    }

    public CompteDTO getCompteSource() {
        return compteSource;
    }

    public void setCompteSource(CompteDTO compteSource) {
        this.compteSource = compteSource;
    }

    public CompteDTO getCompteDestination() {
        return compteDestination;
    }

    public void setCompteDestination(CompteDTO compteDestination) {
        this.compteDestination = compteDestination;
    }

    // Méthodes utilitaires
    public boolean isValidee() {
        return "Validé".equals(statut);
    }

    public boolean isRejetee() {
        return "Rejeté".equals(statut);
    }

    public boolean isEnAttente() {
        return "En attente".equals(statut);
    }

    public boolean isDebit() {
        return "Retrait".equals(type) || "Virement".equals(type);
    }

    public boolean isCredit() {
        return "Dépôt".equals(type);
    }

    public boolean isVirement() {
        return "Virement".equals(type);
    }

    public String getMontantFormate() {
        return String.format("%.2f FCFA", getMontant());
    }

    public String getDateFormatee() {
        if (date != null) {
            return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return "";
    }

    @Override
    public String toString() {
        return "TransactionDTO{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", montant=" + montant +
                ", date=" + date +
                ", statut='" + statut + '\'' +
                ", compteSourceId=" + compteSourceId +
                ", compteDestId=" + compteDestId +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TransactionDTO that = (TransactionDTO) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}