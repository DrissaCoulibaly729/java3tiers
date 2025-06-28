package gm.rahmanproperties.optibank.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTo {
    private Long id;
    private String reference;
    private BigDecimal montant;
    private TypeTransaction type;
    private StatutTransaction statut;
    private Long compteSourceId;
    private Long compteDestinationId;
    private LocalDateTime dateTransaction;
    private String description;
    private String motifRejet;

    public enum TypeTransaction {
        DEPOT,
        RETRAIT,
        VIREMENT,
        PAIEMENT_CARTE
    }

    public enum StatutTransaction {
        EN_ATTENTE,
        VALIDEE,
        REJETEE,
        SUSPECTE
    }
}
