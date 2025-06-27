package com.groupeisi.minisystemebancaire.dto;

public class RemboursementDTO {
    private Long id;
    private double montant;
    private String date;
    private Long creditId;

    // ✅ Constructeurs
    public RemboursementDTO() {}

    public RemboursementDTO(Long id, double montant, String date, Long creditId) {
        this.id = id;
        this.montant = montant;
        this.date = date;
        this.creditId = creditId;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getCreditId() {
        return creditId;
    }

    public void setCreditId(Long creditId) {
        this.creditId = creditId;
    }
}
