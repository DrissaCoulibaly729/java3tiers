package gm.rahmanproperties.optibank.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompteDTo {
    private BigDecimal solde;
    private TypeCompte type;
//    private StatutCompte statut;
    private Long clientId;
//    private String dateCreation;
//    private String dateExpiration;
//    private BigDecimal plafondRetrait;
//    private BigDecimal plafondVirement;

    public enum TypeCompte {
        COURANT,
        EPARGNE
    }
}
