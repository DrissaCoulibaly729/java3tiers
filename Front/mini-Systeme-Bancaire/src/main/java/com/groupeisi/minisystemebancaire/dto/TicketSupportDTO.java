package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;

public class TicketSupportDTO {
    private Long id;

    @SerializedName("client_id")
    private Long clientId;

    private String sujet;
    private String message;
    private String categorie;
    private String statut; // "Ouvert", "En cours", "Résolu", "Fermé"
    private String priorite; // "Basse", "Normale", "Haute", "Urgente"

    @SerializedName("reponse_admin")
    private String reponseAdmin;

    @SerializedName("date_creation")
    private String dateCreation;

    @SerializedName("date_resolution")
    private String dateResolution;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Référence au client
    private ClientDTO client;

    // Constructeurs
    public TicketSupportDTO() {}

    public TicketSupportDTO(Long clientId, String sujet, String message, String categorie) {
        this.clientId = clientId;
        this.sujet = sujet;
        this.message = message;
        this.categorie = categorie;
        this.statut = "Ouvert";
        this.priorite = "Normale";
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getReponseAdmin() { return reponseAdmin; }
    public void setReponseAdmin(String reponseAdmin) { this.reponseAdmin = reponseAdmin; }

    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }

    public String getDateResolution() { return dateResolution; }
    public void setDateResolution(String dateResolution) { this.dateResolution = dateResolution; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public ClientDTO getClient() { return client; }
    public void setClient(ClientDTO client) { this.client = client; }

    // Méthodes utilitaires
    public String getClientNom() {
        return client != null ? client.getNomComplet() : "Client inconnu";
    }

    public boolean isOuvert() {
        return "Ouvert".equals(statut);
    }

    public boolean isEnCours() {
        return "En cours".equals(statut);
    }

    public boolean isResolu() {
        return "Résolu".equals(statut);
    }

    public boolean isFerme() {
        return "Fermé".equals(statut);
    }

    @Override
    public String toString() {
        return "TicketSupportDTO{" +
                "id=" + id +
                ", sujet='" + sujet + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}