package com.groupeisi.minisystemebancaire.dto;

import java.time.LocalDateTime;

public class CreditDTO {
    private Long id;
    private Double montant;
    private Double tauxInteret;
    private Integer dureeMois;
    private Double mensualite;
    private LocalDateTime dateDemande;
    private String statut;
    private Long clientId;
    private ClientDTO client;

    // Constructeurs
    public CreditDTO() {}

    public CreditDTO(Double montant, Double tauxInteret, Integer dureeMois, Long clientId) {
        this.montant = montant;
        this.tauxInteret = tauxInteret;
        this.dureeMois = dureeMois;
        this.clientId = clientId;
        this.statut = "En attente";
        this.dateDemande = LocalDateTime.now();
        this.mensualite = calculerMensualite();
    }

    // Méthode pour calculer la mensualité
    private Double calculerMensualite() {
        if (tauxInteret == null || montant == null || dureeMois == null) return 0.0;

        double tauxMensuel = tauxInteret / 100 / 12;
        if (tauxMensuel == 0) {
            return montant / dureeMois;
        }

        return montant * (tauxMensuel * Math.pow(1 + tauxMensuel, dureeMois)) /
                (Math.pow(1 + tauxMensuel, dureeMois) - 1);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public Double getTauxInteret() { return tauxInteret; }
    public void setTauxInteret(Double tauxInteret) { this.tauxInteret = tauxInteret; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public Double getMensualite() { return mensualite; }
    public void setMensualite(Double mensualite) { this.mensualite = mensualite; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public ClientDTO getClient() { return client; }
    public void setClient(ClientDTO client) { this.client = client; }
}
