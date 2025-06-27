package com.groupeisi.minisystemebancaire.mappers;

import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.models.Client;
import com.groupeisi.minisystemebancaire.models.Compte;

public class CompteMapper {

    public static CompteDTO toDTO(Compte compte) {
        Long clientId = (compte.getClient() != null) ? compte.getClient().getId() : null;
        return new CompteDTO(
                compte.getId(),
                compte.getDateCreation(),
                compte.getNumero(),
                compte.getType(),
                compte.getSolde(),
                clientId,
                compte.getStatut()
        );
    }

    public static Compte toEntity(CompteDTO compteDTO) {
        Compte compte = new Compte();
        compte.setId(compteDTO.getId());
        compte.setDateCreation(compteDTO.getDateCreation());
        compte.setNumero(compteDTO.getNumero());
        compte.setType(compteDTO.getType());
        compte.setSolde(compteDTO.getSolde());
        compte.setStatut(compteDTO.getStatut());
        // 🔍 Vérifie si le ClientID est présent
        if (compteDTO.getClientId() != null) {
            Client client = new Client();
            client.setId(compteDTO.getClientId()); // On ne charge pas toute l'entité, juste l'ID
            compte.setClient(client);
        }
        return compte;
    }
}
