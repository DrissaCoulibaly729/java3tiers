package com.groupeisi.minisystemebancaire.dto;

public class CarteBancaireDTO {
    private Long id;
    private String numero;
    private String type;
    private String cvv;
    private String dateExpiration;
    private double solde;
    private String statut; // "active" ou "bloquée"
    private Long compteId;
    private String codePin;

    // ✅ Constructeurs
    public CarteBancaireDTO() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CarteBancaireDTO(Long id, String numero, String type, String cvv, String dateExpiration, double solde, String statut, Long compteId, String codePin) {
        this.id = id;
        this.numero = numero;
        this.type = type;
        this.cvv = cvv;
        this.dateExpiration = dateExpiration;
        this.solde = solde;
        this.statut = statut;
        this.compteId = compteId;
        this.codePin = codePin;
    }

    // ✅ Getters & Setters

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

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(String dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Long getCompteId() {
        return compteId;
    }

    public void setCompteId(Long compteId) {
        this.compteId = compteId;
    }

    public String getCodePin() {
        return codePin;
    }

    public void setCodePin(String codePin) {
        this.codePin = codePin;
    }
}
