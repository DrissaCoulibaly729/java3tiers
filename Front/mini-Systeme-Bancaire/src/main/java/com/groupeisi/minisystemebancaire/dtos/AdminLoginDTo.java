package com.groupeisi.minisystemebancaire.dtos;

/**
 * DTO pour la réponse de connexion admin
 */
public class AdminLoginDTo {
    private Long id;
    private String username;
    private String role;

    // Constructeurs
    public AdminLoginDTo() {}

    public AdminLoginDTo(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Getters et Setters
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "AdminLoginDTo{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}