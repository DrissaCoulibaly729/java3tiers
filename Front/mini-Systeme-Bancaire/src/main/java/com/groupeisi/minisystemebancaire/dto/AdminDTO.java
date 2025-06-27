package com.groupeisi.minisystemebancaire.dto;

public class AdminDTO {
    private Long id;
    private String username;
    private String password; // Ajout du mot de passe
    private String role; // "Admin" ou "Gestionnaire"

    // ✅ Constructeurs
    public AdminDTO() {}

    public AdminDTO(Long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    public AdminDTO(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // ✅ Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
