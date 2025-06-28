package com.groupeisi.minisystemebancaire.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTo {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String cni;
    private String statut; // "Actif", "Suspendu"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}