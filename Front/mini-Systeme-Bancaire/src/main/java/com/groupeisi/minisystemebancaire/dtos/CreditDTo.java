package gm.rahmanproperties.optibank.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreditDTo {
    private Long id;
    private String reference;
    private BigDecimal montant;
    private BigDecimal tauxAnnuel;
    private Integer dureeEnMois;
    private BigDecimal mensualite;
    private StatutCredit statut;
    private Long clientId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String motifRejet;
    private BigDecimal montantRestant;

    public enum StatutCredit {
        EN_ATTENTE,
        APPROUVE,
        REJETE,
        EN_COURS,
        TERMINE,
        EN_RETARD
    }
}
