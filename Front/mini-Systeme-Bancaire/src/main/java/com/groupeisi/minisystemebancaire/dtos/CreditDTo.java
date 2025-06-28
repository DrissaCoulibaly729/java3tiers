package com.groupeisi.minisystemebancaire.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDTo {
    private Long id;
    private BigDecimal montant;
    private BigDecimal tauxInteret;
    private Integer dureeRemboursement; // en mois
    private String statut; // "En attente", "Accepté", "Refusé"
    private String motif;
    private Long clientId;
    private ClientDTo client;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}