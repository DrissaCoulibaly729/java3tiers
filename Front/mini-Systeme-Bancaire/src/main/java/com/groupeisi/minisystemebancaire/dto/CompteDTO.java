package com.groupeisi.minisystemebancaire.dto;

import java.time.LocalDateTime;

public class CompteDTO {
    private Long id;
    private String numero;
    private String type;
    private Double solde;
    private LocalDateTime dateCreation;
    private String statut;
    private Long clientId;
    private ClientDTO client;

    // Constructeurs
    public CompteDTO() {}

    public CompteDTO(String type, Double solde, Long clientId) {
        this.type = type;
        this.solde = solde;
        this.clientId = clientId;
        this.statut = "Actif";
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getSolde() { return solde; }
    public void setSolde(Double solde) { this.solde = solde; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public ClientDTO getClient() { return client; }
    public void setClient(ClientDTO client) { this.client = client; }

    @Override
    public String toString() {
        return numero + " - " + type + " (" + solde + "€)";
    }
}