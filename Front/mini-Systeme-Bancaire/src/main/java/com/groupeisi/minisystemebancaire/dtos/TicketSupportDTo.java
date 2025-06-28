package gm.rahmanproperties.optibank.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketSupportDTo {
    private Long id;
    private String reference;
    private String sujet;
    private String description;
    private String reponse;
    private PrioriteTicket priorite;
    private StatutTicket statut;
    private Long clientId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    public enum PrioriteTicket {
        BASSE,
        MOYENNE,
        HAUTE,
        URGENTE
    }

    public enum StatutTicket {
        OUVERT,
        EN_COURS,
        RESOLU,
        FERME,
        EN_ATTENTE
    }
}
