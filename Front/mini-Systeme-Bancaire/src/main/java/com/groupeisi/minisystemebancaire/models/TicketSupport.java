package com.groupeisi.minisystemebancaire.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TicketSupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sujet;
    private String description;
    private LocalDateTime dateOuverture;
    private String statut; // "Ouvert", "Résolu"

    @Column(length = 1000) // Stocker la réponse de l'admin
    private String reponse;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;

    // Constructeurs, Getters et Setters

    public TicketSupport() {
    }

    public TicketSupport(Long id, String sujet, String description, LocalDateTime dateOuverture, String statut, String reponse, Client client, Admin admin) {
        this.id = id;
        this.sujet = sujet;
        this.description = description;
        this.dateOuverture = dateOuverture;
        this.statut = statut;
        this.reponse = reponse;
        this.client = client;
        this.admin = admin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }
}
