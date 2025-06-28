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
    private String typeCompte; // "Épargne", "Courant"
    private BigDecimal solde;
    private String statut; // "Actif", "Fermé", "Suspendu"
    private Long clientId;
    private ClientDTo client;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
