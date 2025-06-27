package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransactionDTO {
    private Long id;
    private String type; // "Dépôt", "Retrait", "Virement"
    private double montant;
    private LocalDateTime date; // ✅ Utilisation de LocalDateTime
    @SerializedName("compte_source_id")
    private Long compteSourceId;
    @SerializedName("compte_dest_id")
    private Long compteDestId;
    private String statut; // "validé" ou "rejeté"

    // ✅ Constructeurs
    public TransactionDTO() {}

    public TransactionDTO(Long id, String type, double montant, LocalDateTime date, Long compteSourceId, Long compteDestId, String statut) {
        this.id = id;
        this.type = type;
        this.montant = montant;
        this.date = date;
        this.compteSourceId = compteSourceId;
        this.compteDestId = compteDestId;
        this.statut = statut;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getCompteSourceId() {
        return compteSourceId;
    }

    public void setCompteSourceId(Long compteSourceId) {
        this.compteSourceId = compteSourceId;
    }

    public Long getCompteDestId() {
        return compteDestId;
    }

    public void setCompteDestId(Long compteDestId) {
        this.compteDestId = compteDestId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    // ✅ Méthodes de conversion pour JSON (si nécessaire)
    public String getDateFormatted() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
    }

    public void setDateFromString(String dateString) {
        this.date = dateString != null ? LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
    }
}
