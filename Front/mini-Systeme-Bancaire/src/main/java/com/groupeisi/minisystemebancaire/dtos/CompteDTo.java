package com.groupeisi.minisystemebancaire.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteDTo {
    private Long id;
    private String numeroCompte;
    private String type; // courant, epargne, etc.
    private BigDecimal solde;
    private Boolean actif;
    private Long clientId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}