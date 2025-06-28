package com.groupeisi.minisystemebancaire.dto;

/**
 * ✅ DTO pour la classe Admin
 */
public class AdminDTO {
    private Long id;
    private String username;
    private String password;
    private String role;

    // Constructeurs
    public AdminDTO() {}

    public AdminDTO(String username, String password, String role) {
        this.username = username;
        this.password = password;
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

    // Méthodes utilitaires
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isGestionnaire() {
        return "GESTIONNAIRE".equals(role);
    }

    @Override
    public String toString() {
        return "AdminDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}