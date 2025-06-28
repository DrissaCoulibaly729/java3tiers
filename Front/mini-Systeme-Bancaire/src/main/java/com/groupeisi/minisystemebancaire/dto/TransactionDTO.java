package com.groupeisi.minisystemebancaire.dto;

import java.time.LocalDateTime;

public class TransactionDTO {
    private Long id;
    private String type;
    private Double montant;
    private LocalDateTime date;
    private Long compteSourceId;
    private Long compteDestId;
    private String statut;
    private String description;
    private CompteDTO compteSource;
    private CompteDTO compteDestination;

    // Constructeurs
    public TransactionDTO() {}

    public TransactionDTO(String type, Double montant, Long compteSourceId, Long compteDestId, String description) {
        this.type = type;
        this.montant = montant;
        this.compteSourceId = compteSourceId;
        this.compteDestId = compteDestId;
        this.description = description;
        this.statut = "Validé";
        this.date = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Long getCompteSourceId() { return compteSourceId; }
    public void setCompteSourceId(Long compteSourceId) { this.compteSourceId = compteSourceId; }

    public Long getCompteDestId() { return compteDestId; }
    public void setCompteDestId(Long compteDestId) { this.compteDestId = compteDestId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CompteDTO getCompteSource() { return compteSource; }
    public void setCompteSource(CompteDTO compteSource) { this.compteSource = compteSource; }

    public CompteDTO getCompteDestination() { return compteDestination; }
    public void setCompteDestination(CompteDTO compteDestination) { this.compteDestination = compteDestination; }
}