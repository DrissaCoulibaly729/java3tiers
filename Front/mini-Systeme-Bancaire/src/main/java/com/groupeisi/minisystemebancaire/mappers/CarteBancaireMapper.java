package com.groupeisi.minisystemebancaire.mappers;

import com.groupeisi.minisystemebancaire.dto.CarteBancaireDTO;
import com.groupeisi.minisystemebancaire.models.CarteBancaire;
import com.groupeisi.minisystemebancaire.models.Client;
import com.groupeisi.minisystemebancaire.models.Compte;

public class CarteBancaireMapper {

    public static CarteBancaireDTO toDTO(CarteBancaire carte) {
        return new CarteBancaireDTO(
                carte.getId(),
                carte.getNumero(),
                carte.getType(), // ✅ Correction : Ajout du champ `type`
                carte.getCvv(),
                carte.getDateExpiration(),
                carte.getSolde(),
                carte.getStatut(),
                carte.getCompte().getId(),
                carte.getCodePin()
        );
    }

    public static CarteBancaire toEntity(CarteBancaireDTO carteDTO) {
        CarteBancaire carte = new CarteBancaire();
        carte.setId(carteDTO.getId());
        carte.setNumero(carteDTO.getNumero());
        carte.setCvv(carteDTO.getCvv());
        carte.setDateExpiration(carteDTO.getDateExpiration());
        carte.setSolde(carteDTO.getSolde());
        carte.setStatut(carteDTO.getStatut());
        carte.setCodePin(carteDTO.getCodePin());

        // Vérifier si l'ID du client est disponible, et créer un objet Client
        if (carteDTO.getCompteId() != null) {
            Compte compte = new Compte();  // Créer un objet Client
            compte.setId(carteDTO.getCompteId());  // Assigner l'ID
            carte.setCompte(compte);  // Associer au crédit
        } else {
            carte.setCompte(null);
        }
        return carte;
    }
}
