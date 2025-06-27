package com.groupeisi.minisystemebancaire.mappers;

import com.groupeisi.minisystemebancaire.dto.TicketSupportDTO;
import com.groupeisi.minisystemebancaire.models.Client;
import com.groupeisi.minisystemebancaire.models.TicketSupport;

public class TicketSupportMapper {

    public static TicketSupportDTO toDTO(TicketSupport ticket) {
        return new TicketSupportDTO(
                ticket.getId(),
                ticket.getSujet(),
                ticket.getDescription(),
                ticket.getDateOuverture(),
                ticket.getStatut(),
                ticket.getReponse(),
                ticket.getClient().getId(),
                ticket.getAdmin() != null ? ticket.getAdmin().getId() : null
        );
    }

    public static TicketSupport toEntity(TicketSupportDTO ticketDTO) {
        TicketSupport ticket = new TicketSupport();
        ticket.setId(ticketDTO.getId());
        ticket.setSujet(ticketDTO.getSujet());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setDateOuverture(ticketDTO.getDateOuverture());
        ticket.setStatut(ticketDTO.getStatut());

        // Vérifier si l'ID du client est disponible, et créer un objet Client
        if (ticketDTO.getClientId() != null) {
            Client client = new Client();  // Créer un objet Client
            client.setId(ticketDTO.getClientId());  // Assigner l'ID
            ticket.setClient(client);  // Associer au crédit
        } else {
            ticket.setClient(null);
        }
        return ticket;
    }
}
