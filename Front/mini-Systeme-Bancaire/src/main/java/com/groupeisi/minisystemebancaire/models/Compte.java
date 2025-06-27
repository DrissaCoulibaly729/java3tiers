package com.groupeisi.minisystemebancaire.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Compte{
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String numero;
private String type; // "courant" ou "épargne"
private double solde;
private LocalDateTime dateCreation;
private String statut; // "Actif" ou "Bloqué"

@ManyToOne
@JoinColumn(name = "client_id")
private Client client;

@OneToMany(mappedBy = "compteSource", cascade = CascadeType.ALL)
private List<Transaction> transactionsEnvoyees;

@OneToMany(mappedBy = "compteDest", cascade = CascadeType.ALL)
private List<Transaction> transactionsRecues;

@OneToMany(mappedBy = "compte", cascade = CascadeType.ALL)
private List<CarteBancaire> cartesBancaires;

// Constructeurs, Getters et Setters{


    public Compte() {
    }

    public Compte(Long id, String numero, String type, double solde, LocalDateTime dateCreation, String statut, Client client, List<Transaction> transactionsEnvoyees, List<Transaction> transactionsRecues, List<CarteBancaire> cartesBancaires) {
        this.id = id;
        this.numero = numero;
        this.type = type;
        this.solde = solde;
        this.dateCreation = dateCreation;
        this.statut = statut;
        this.client = client;
        this.transactionsEnvoyees = transactionsEnvoyees;
        this.transactionsRecues = transactionsRecues;
        this.cartesBancaires = cartesBancaires;
    }

    public Long getId() {
        return id;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getSolde() {
        return solde;
    }

    public void setSolde(double solde) {
        this.solde = solde;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<Transaction> getTransactionsEnvoyees() {
        return transactionsEnvoyees;
    }

    public void setTransactionsEnvoyees(List<Transaction> transactionsEnvoyees) {
        this.transactionsEnvoyees = transactionsEnvoyees;
    }

    public List<Transaction> getTransactionsRecues() {
        return transactionsRecues;
    }

    public void setTransactionsRecues(List<Transaction> transactionsRecues) {
        this.transactionsRecues = transactionsRecues;
    }

    public List<CarteBancaire> getCartesBancaires() {
        return cartesBancaires;
    }

    public void setCartesBancaires(List<CarteBancaire> cartesBancaires) {
        this.cartesBancaires = cartesBancaires;
    }
}
