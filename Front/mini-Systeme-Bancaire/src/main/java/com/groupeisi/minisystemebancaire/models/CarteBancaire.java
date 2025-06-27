package com.groupeisi.minisystemebancaire.models;


import jakarta.persistence.*;

@Entity
public class CarteBancaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;
    private String type;
    private String cvv;
    private String dateExpiration;
    private double solde;
    private String statut; // "active", "bloquée"

    @ManyToOne
    @JoinColumn(name = "compte_id")
    private Compte compte;

    private String codePin;

    //constructeur getter setter


    public CarteBancaire() {
    }

    public CarteBancaire(Long id, String numero, String type, String cvv, String dateExpiration, double solde, String statut, Compte compte, String codePin) {
        this.id = id;
        this.numero = numero;
        this.type = type;
        this.cvv = cvv;
        this.dateExpiration = dateExpiration;
        this.solde = solde;
        this.statut = statut;
        this.compte = compte;
        this.codePin = codePin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public String getCodePin() {
        return codePin;
    }

    public void setCodePin(String codePin) {
        this.codePin = codePin;
    }
}
