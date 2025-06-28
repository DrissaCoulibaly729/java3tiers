package gm.rahmanproperties.optibank.dtos;

import lombok.Data;

@Data
public class AdminDTo {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String username;
    private boolean actif;
}
