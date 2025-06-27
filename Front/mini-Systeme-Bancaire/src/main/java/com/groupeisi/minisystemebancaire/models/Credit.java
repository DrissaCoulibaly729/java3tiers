package com.groupeisi.minisystemebancaire.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Credit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double montant;
    private double tauxInteret;
    private int dureeMois;
    private double mensualite;
    private LocalDateTime dateDemande;
    private String statut; // "En attente", "Approuvé", "Refusé"

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "credit", cascade = CascadeType.ALL)
    private List<Remboursement> remboursements;

    // Constructeurs, Getters et Setters

    public Credit() {
    }

    public Credit(Long id, double montant, double tauxInteret, int dureeMois, double mensualite, LocalDateTime dateDemande, String statut, Client client, List<Remboursement> remboursements) {
        this.id = id;
        this.montant = montant;
        this.tauxInteret = tauxInteret;
        this.dureeMois = dureeMois;
        this.mensualite = mensualite;
        this.dateDemande = dateDemande;
        this.statut = statut;
        this.client = client;
        this.remboursements = remboursements;
    }

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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<Remboursement> getRemboursements() {
        return remboursements;
    }

    public void setRemboursements(List<Remboursement> remboursements) {
        this.remboursements = remboursements;
    }
}
