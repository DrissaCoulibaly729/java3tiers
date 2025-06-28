package gm.rahmanproperties.optibank.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class ClientDTo {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
//    private String dateNaissance;
//    private String profession;
    private Set<Role> roles = Set.of(Role.ROLE_CLIENT);
    private String username;
    private String password;
//    private StatutClient statut;

    public enum StatutClient {
        ACTIF,
        INACTIF,
        BLOQUE,
        EN_ATTENTE
    }

    public enum Role {
        ROLE_CLIENT,
        ROLE_ADMIN,
        ROLE_AGENT
    }
}
