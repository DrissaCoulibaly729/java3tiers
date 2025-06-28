package com.groupeisi.minisystemebancaire.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTo {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String role; // "Super Admin", "Admin", "Gestionnaire"
    private String statut; // "Actif", "Inactif"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}