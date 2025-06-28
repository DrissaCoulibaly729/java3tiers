package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

/**
 * ✅ DTO pour la classe Compte
 */
public class CompteDTO {
    private Long id;
    private String numero;
    private String type; // "Courant" ou "Épargne"
    private Double solde;
    private String statut; // "Actif" ou "Fermé"

    @SerializedName("date_creation")
    private LocalDateTime dateCreation;

    @SerializedName("client_id")
    private Long clientId;

    // Référence au client (pour les jointures)
    private ClientDTO client;

    // Constructeurs
    public CompteDTO() {}

    public CompteDTO(String numero, String type, Double solde, Long clientId) {
        this.numero = numero;
        this.type = type;
        this.solde = solde;
        this.clientId = clientId;
        this.statut = "Actif";
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getSolde() {
        return solde != null ? solde : 0.0;
    }

    public void setSolde(Double solde) {
        this.solde = solde;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public ClientDTO getClient() {
        return client;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }

    // Méthodes utilitaires
    public boolean isActif() {
        return "Actif".equals(statut);
    }

    public boolean isFerme() {
        return "Fermé".equals(statut);
    }

    public boolean isCourant() {
        return "Courant".equals(type);
    }

    public boolean isEpargne() {
        return "Épargne".equals(type);
    }

    public String getSoldeFormate() {
        return String.format("%.2f FCFA", getSolde());
    }

    public String getClientNom() {
        if (client != null) {
            return client.getNomComplet();
        }
        return "Client ID: " + clientId;
    }

    public void crediter(double montant) {
        if (montant > 0) {
            this.solde = getSolde() + montant;
        }
    }

    public boolean debiter(double montant) {
        if (montant > 0 && getSolde() >= montant) {
            this.solde = getSolde() - montant;
            return true;
        }
        return false;
    }

    public boolean peutDebiter(double montant) {
        return montant > 0 && getSolde() >= montant;
    }

    @Override
    public String toString() {
        return "CompteDTO{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", type='" + type + '\'' +
                ", solde=" + solde +
                ", statut='" + statut + '\'' +
                ", clientId=" + clientId +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompteDTO compteDTO = (CompteDTO) obj;
        return id != null && id.equals(compteDTO.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}