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
    private String type; // depot, retrait, virement, etc.
    private BigDecimal montant;
    private String description;
    private String statut; // en_attente, validee, annulee
    private Long compteSourceId;
    private Long compteDestinationId;
    private LocalDateTime dateTransaction;
    private LocalDateTime dateCreation;
}