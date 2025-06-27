package com.groupeisi.minisystemebancaire.models;

import jakarta.persistence.*;

@Entity
public class Remboursement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double montant;
    private String date;

    @ManyToOne
    @JoinColumn(name = "credit_id")
    private Credit credit;

    // Constructeurs, Getters et Setters

    public Remboursement() {
    }

    public Remboursement(Long id, double montant, String date, Credit credit) {
        this.id = id;
        this.montant = montant;
        this.date = date;
        this.credit = credit;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Credit getCredit() {
        return credit;
    }

    public void setCredit(Credit credit) {
        this.credit = credit;
    }
}
