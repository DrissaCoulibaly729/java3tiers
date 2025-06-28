package com.groupeisi.minisystemebancaire.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraisBancaireDTo {
    private Long id;
    private String libelle;
    private String description;
    private BigDecimal montant;
    private String type; // fixe, pourcentage
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}