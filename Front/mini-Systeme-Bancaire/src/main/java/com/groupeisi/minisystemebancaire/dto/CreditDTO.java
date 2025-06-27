package com.groupeisi.minisystemebancaire.dto;

import java.time.LocalDateTime;

public class CreditDTO {
    private Long id;
    private double montant;
    private double tauxInteret;
    private int dureeMois;
    private double mensualite;
    private LocalDateTime dateDemande;
    private String statut; // "En attente", "Approuvé", "Refusé"
    private Long clientId;

    // ✅ Constructeurs
    public CreditDTO() {}

    public CreditDTO(Long id, double montant, double tauxInteret, int dureeMois, double mensualite, LocalDateTime dateDemande, String statut, Long clientId) {
        this.id = id;
        this.montant = montant;
        this.tauxInteret = tauxInteret;
        this.dureeMois = dureeMois;
        this.mensualite = mensualite;
        this.dateDemande = dateDemande;
        this.statut = statut;
        this.clientId = clientId;
    }

    // ✅ Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public double getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(double tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public int getDureeMois() {
        return dureeMois;
    }

    public void setDureeMois(int dureeMois) {
        this.dureeMois = dureeMois;
    }

    public double getMensualite() {
        return mensualite;
    }

    public void setMensualite(double mensualite) {
        this.mensualite = mensualite;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}
