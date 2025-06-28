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
public class CarteBancaireDTo {
    private Long id;
    private String numeroCarte;
    private String type; // debit, credit
    private LocalDate dateExpiration;
    private String statut; // active, bloquee, expiree
    private BigDecimal plafondJournalier;
    private BigDecimal plafondMensuel;
    private Long compteId;
    private LocalDateTime dateCreation;
}
