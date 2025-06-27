package com.groupeisi.minisystemebancaire.dto;

import java.time.LocalDateTime;

public class TicketSupportDTO {
    private Long id;
    private String sujet;
    private String description;
    private LocalDateTime dateOuverture;
    private String statut; // "Ouvert", "Résolu"
    private String reponse;
    private Long clientId;
    private Long adminId;

    // ✅ Constructeurs
    public TicketSupportDTO() {}

    public TicketSupportDTO(Long id, String sujet, String description, LocalDateTime dateOuverture, String statut, String reponse, Long clientId, Long adminId) {
        this.id = id;
        this.sujet = sujet;
        this.description = description;
        this.dateOuverture = dateOuverture;
        this.statut = statut;
        this.reponse = reponse;
        this.clientId = clientId;
        this.adminId = adminId;
    }

    // ✅ Getters & Setters

    public Long getId() {
        return id;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateOuverture() {
        return dateOuverture;
    }

    public void setDateOuverture(LocalDateTime dateOuverture) {
        this.dateOuverture = dateOuverture;
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

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
}
