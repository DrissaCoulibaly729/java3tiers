package com.groupeisi.minisystemebancaire.dto;

import java.time.LocalDate;

public class CarteBancaireDTO {
    private Long id;
    private String numero;
    private String cvv;
    private LocalDate dateExpiration;
    private Double solde;
    private String statut;
    private Long compteId;
    private String codePin;
    private CompteDTO compte;

    // ✅ Constructeur par défaut (obligatoire)
    public CarteBancaireDTO() {}

    // ✅ Constructeur minimal (pour création)
    public CarteBancaireDTO(Double solde, Long compteId) {
        this.solde = solde;
        this.compteId = compteId;
        this.statut = "Active";
    }

    // ✅ Constructeur complet (pour les cas où on a toutes les données)
    public CarteBancaireDTO(Long id, String numero, String cvv, LocalDate dateExpiration,
                            Double solde, String statut, Long compteId, String codePin) {
        this.id = id;
        this.numero = numero;
        this.cvv = cvv;
        this.dateExpiration = dateExpiration;
        this.solde = solde;
        this.statut = statut;
        this.compteId = compteId;
        this.codePin = codePin;
    }

    // ✅ Constructeur pour création avec toutes les données (ce qui manquait)
    public CarteBancaireDTO(String numero, String cvv, LocalDate dateExpiration,
                            Double solde, String statut, Long compteId, String codePin) {
        this.numero = numero;
        this.cvv = cvv;
        this.dateExpiration = dateExpiration;
        this.solde = solde;
        this.statut = statut;
        this.compteId = compteId;
        this.codePin = codePin;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

    public Double getSolde() { return solde; }
    public void setSolde(Double solde) { this.solde = solde; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Long getCompteId() { return compteId; }
    public void setCompteId(Long compteId) { this.compteId = compteId; }

    public String getCodePin() { return codePin; }
    public void setCodePin(String codePin) { this.codePin = codePin; }

    public CompteDTO getCompte() { return compte; }
    public void setCompte(CompteDTO compte) { this.compte = compte; }

    @Override
    public String toString() {
        return "**** **** **** " + (numero != null && numero.length() >= 4 ? numero.substring(12) : "****");
    }
}