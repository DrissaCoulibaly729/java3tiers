package gm.rahmanproperties.optibank.dtos;

import lombok.Data;

@Data
public class CarteBancaireDTo {
    private Long id;
    private String numero;
    private String titulaire;
    private String dateExpiration;
    private String cvv;
    private StatutCart statut;
    private Long compteId;

    public enum StatutCart {
        ACTIVE,
        BLOQUEE,
        PERDUE,
        VOLEE,
        EXPIREE
    }
}

