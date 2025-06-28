package com.groupeisi.minisystemebancaire.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSupportDTo {
    private Long id;
    private String sujet;
    private String message;
    private String statut; // ouvert, en_cours, ferme
    private String priorite; // basse, normale, haute, urgente
    private Long clientId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
