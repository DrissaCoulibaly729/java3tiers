package com.groupeisi.minisystemebancaire.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@NamedQuery(name = "listClients", query = "SELECT c FROM Client c ORDER BY c.nom ASC")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;  // Utilisé pour la connexion

    private String telephone;
    private String adresse;
    private String statut; // Actif, Suspendu

    @Column(nullable = false)
    private String password;  // Mot de passe sécurisé

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Compte> comptes;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Credit> credits;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<TicketSupport> tickets;

    // ✅ Constructeurs
    public Client() {}

    public Client(String nom, String prenom, String email, String telephone, String adresse, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.password = password;
        this.statut = "Actif";  // Par défaut, un client est actif
    }

    // ✅ Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Compte> getComptes() {
        return comptes;
    }

    public void setComptes(List<Compte> comptes) {
        this.comptes = comptes;
    }

    public List<Credit> getCredits() {
        return credits;
    }

    public void setCredits(List<Credit> credits) {
        this.credits = credits;
    }

    public List<TicketSupport> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketSupport> tickets) {
        this.tickets = tickets;
    }
}
