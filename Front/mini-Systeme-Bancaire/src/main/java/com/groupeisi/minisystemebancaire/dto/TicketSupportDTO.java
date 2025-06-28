package com.groupeisi.minisystemebancaire.dto;

import java.time.LocalDateTime;

public class TicketSupportDTO {
    private Long id;
    private String sujet;
    private String description;
    private LocalDateTime dateOuverture;
    private String statut;
    private String reponse;
    private Long clientId;
    private Long adminId;
    private ClientDTO client;

    // Constructeurs
    public TicketSupportDTO() {}

    public TicketSupportDTO(String sujet, String description, Long clientId) {
        this.sujet = sujet;
        this.description = description;
        this.clientId = clientId;
        this.statut = "Ouvert";
        this.dateOuverture = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateOuverture() { return dateOuverture; }
    public void setDateOuverture(LocalDateTime dateOuverture) { this.dateOuverture = dateOuverture; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public ClientDTO getClient() { return client; }
    public void setClient(ClientDTO client) { this.client = client; }
}