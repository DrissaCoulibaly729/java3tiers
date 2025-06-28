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
    private String typeFrais; // "Maintenance", "Virement", "Retrait", "Découvert"
    private BigDecimal montant;
    private String description;
    private Long compteId;
    private CompteDTo compte;
    private LocalDateTime createdAt;
}