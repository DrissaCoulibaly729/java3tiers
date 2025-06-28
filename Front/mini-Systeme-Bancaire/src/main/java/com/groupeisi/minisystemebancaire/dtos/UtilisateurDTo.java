package gm.rahmanproperties.optibank.dtos;

import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class UtilisateurDTo {
    private Long id;
    private String username;
    private String email;
    private Set<Role> roles = new HashSet<>();
    private StatutUtilisateur statut;
    private boolean premierConnexion;

    public enum Role {
        ROLE_ADMIN,
        ROLE_CLIENT
    }

    public enum StatutUtilisateur {
        ACTIF,
        INACTIF,
        BLOQUE
    }
}
