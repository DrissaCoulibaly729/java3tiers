package com.groupeisi.minisystemebancaire.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role; // "Admin", "Gestionnaire"

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<TicketSupport> ticketsGeres;

    // Constructeurs, Getters et Setters


    public Admin() {
    }

    public Admin(Long id, String username, String password, String role, List<TicketSupport> ticketsGeres) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.ticketsGeres = ticketsGeres;
    }

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

    public List<TicketSupport> getTicketsGeres() {
        return ticketsGeres;
    }

    public void setTicketsGeres(List<TicketSupport> ticketsGeres) {
        this.ticketsGeres = ticketsGeres;
    }
}
