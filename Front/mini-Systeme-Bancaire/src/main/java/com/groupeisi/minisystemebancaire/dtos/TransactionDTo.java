package com.groupeisi.minisystemebancaire.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTo {
    private Long id;
    private String type; // "Dépôt", "Retrait", "Virement"
    private BigDecimal montant;
    private String description;
    private String statut; // "Effectué", "En cours", "Rejeté"
    private Long compteSourceId;
    private Long compteDestId;
    private CompteDTo compteSource;
    private CompteDTo compteDestination;
    private LocalDateTime createdAt;
}