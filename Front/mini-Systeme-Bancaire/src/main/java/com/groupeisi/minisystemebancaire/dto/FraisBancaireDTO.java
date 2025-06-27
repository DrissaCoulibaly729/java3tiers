package com.groupeisi.minisystemebancaire.dto;

public class FraisBancaireDTO {
    private Long id;
    private String type; // "Mensuel", "Retrait", "Virement"
    private double montant;
    private String dateApplication;
    private Long compteId;

    // ✅ Constructeurs
    public FraisBancaireDTO() {}

    public FraisBancaireDTO(Long id, String type, double montant, String dateApplication, Long compteId) {
        this.id = id;
        this.type = type;
        this.montant = montant;
        this.dateApplication = dateApplication;
        this.compteId = compteId;
    }

    // ✅ Getters & Setters

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

    public String getDateApplication() {
        return dateApplication;
    }

    public void setDateApplication(String dateApplication) {
        this.dateApplication = dateApplication;
    }

    public Long getCompteId() {
        return compteId;
    }

    public void setCompteId(Long compteId) {
        this.compteId = compteId;
    }
}
