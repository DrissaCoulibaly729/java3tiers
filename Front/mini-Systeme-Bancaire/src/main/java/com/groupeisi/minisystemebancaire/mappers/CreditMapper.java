package com.groupeisi.minisystemebancaire.mappers;

import com.groupeisi.minisystemebancaire.dto.CreditDTO;
import com.groupeisi.minisystemebancaire.models.Client;
import com.groupeisi.minisystemebancaire.models.Credit;

public class CreditMapper {

    public static CreditDTO toDTO(Credit credit) {
        if (credit == null) return null;

        // Vérifier si le client est null avant d'accéder à son ID
        Long clientId = (credit.getClient() != null) ? credit.getClient().getId() : null;
        return new CreditDTO(
                credit.getId(),
                credit.getMontant(),
                credit.getTauxInteret(),
                credit.getDureeMois(),
                credit.getMensualite(),
                credit.getDateDemande(),
                credit.getStatut(),
                clientId
        );
    }

    public static Credit toEntity(CreditDTO creditDTO) {
        Credit credit = new Credit();
        credit.setId(creditDTO.getId());
        credit.setMontant(creditDTO.getMontant());
        credit.setTauxInteret(creditDTO.getTauxInteret());
        credit.setDureeMois(creditDTO.getDureeMois());
        credit.setMensualite(creditDTO.getMensualite());
        credit.setDateDemande(creditDTO.getDateDemande());
        credit.setStatut(creditDTO.getStatut());
        // Vérifier si l'ID du client est disponible, et créer un objet Client
        if (creditDTO.getClientId() != null) {
            Client client = new Client();  // Créer un objet Client
            client.setId(creditDTO.getClientId());  // Assigner l'ID
            credit.setClient(client);  // Associer au crédit
        } else {
            credit.setClient(null);
        }
        return credit;
    }
}
