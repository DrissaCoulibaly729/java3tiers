package com.groupeisi.minisystemebancaire.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "Dépôt", "Retrait", "Virement"
    private double montant;

    @Column(nullable = false)
    private LocalDateTime date; // Utilisation de LocalDateTime pour stocker la date

    private String statut; // "validé", "rejeté"

    @ManyToOne
    @JoinColumn(name = "compte_source_id")
    private Compte compteSource;

    @ManyToOne
    @JoinColumn(name = "compte_dest_id")
    private Compte compteDest;

    // Constructeurs, Getters et Setters

    public Transaction() {
    }

    public Transaction(Long id, String type, double montant, LocalDateTime date, String statut, Compte compteSource, Compte compteDest) {
        this.id = id;
        this.type = type;
        this.montant = montant;
        this.date = date;
        this.statut = statut;
        this.compteSource = compteSource;
        this.compteDest = compteDest;
    }

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

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
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

    public Compte getCompteSource() {
        return compteSource;
    }

    public void setCompteSource(Compte compteSource) {
        this.compteSource = compteSource;
    }

    public Compte getCompteDest() {
        return compteDest;
    }

    public void setCompteDest(Compte compteDest) {
        this.compteDest = compteDest;
    }
}
