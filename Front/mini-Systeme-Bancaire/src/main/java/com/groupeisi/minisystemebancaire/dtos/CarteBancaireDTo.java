package com.groupeisi.minisystemebancaire.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarteBancaireDTo {
    private Long id;
    private String numeroCarte;
    private String typeCarte; // "Débit", "Crédit"
    private LocalDate dateExpiration;
    private String statut; // "Active", "Bloquée", "Expirée"
    private Long compteId;
    private CompteDTo compte;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}