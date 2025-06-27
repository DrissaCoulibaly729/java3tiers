package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class CompteDTO {
    private Long id;
    @SerializedName("date_creation")
    private LocalDateTime dateCreation;
    private String numero;
    private String type; // "courant" ou "épargne"
    private double solde;
    @SerializedName("client_id")
    private Long clientId;
    private String statut; // "Actif" ou "Bloqué"

    // ✅ Constructeurs
    public CompteDTO() {}

    public CompteDTO(Long id, LocalDateTime dateCreation, String numero, String type, double solde, Long clientId, String statut) {
        this.id = id;
        this.dateCreation = dateCreation;
        this.numero = numero;
        this.type = type;
        this.solde = solde;
        this.clientId = clientId;
        this.statut = statut;
    }


// ✅ Getters & Setters


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

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
