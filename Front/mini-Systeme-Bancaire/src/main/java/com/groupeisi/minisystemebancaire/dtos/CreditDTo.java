package com.groupeisi.minisystemebancaire.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDTo {
    private Long id;
    private BigDecimal montant;
    private BigDecimal tauxInteret;
    private Integer dureeEnMois;
    private BigDecimal mensualite;
    private String statut; // en_attente, approuve, refuse, rembourse
    private String motif;
    private Long clientId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private LocalDateTime dateCreation;
}