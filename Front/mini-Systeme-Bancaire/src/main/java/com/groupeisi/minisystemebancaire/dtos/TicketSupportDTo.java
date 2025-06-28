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
    private String description;
    private String statut; // "Ouvert", "En cours", "Résolu", "Fermé"
    private String priorite; // "Basse", "Normale", "Haute", "Urgente"
    private String reponse;
    private Long clientId;
    private ClientDTo client;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}