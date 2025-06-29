package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * ✅ DTO pour la classe Client - Version corrigée pour les dates
 */
public class ClientDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String password;
    private String statut;

    // ✅ CHANGEMENT: Gestion flexible des dates pour éviter les erreurs de parsing
    @SerializedName("date_inscription")
    private String dateInscriptionStr; // Pour recevoir depuis l'API

    private LocalDateTime dateInscription; // Pour usage interne

    @SerializedName("created_at")
    private String createdAtStr;

    @SerializedName("updated_at")
    private String updatedAtStr;

    private List<CompteDTO> comptes;

    // ✅ Formatters pour les dates
    private static final DateTimeFormatter LARAVEL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
    private static final DateTimeFormatter SIMPLE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // Constructeurs
    public ClientDTO() {}

    // ✅ CORRECTION: Constructeur avec 6 paramètres comme attendu par votre code
    public ClientDTO(String nom, String prenom, String email, String telephone, String adresse, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.password = password;
        this.statut = "Actif";
        this.dateInscription = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateInscription() {
        // ✅ CORRECTION: Parser automatiquement si on a une string
        if (dateInscription == null && dateInscriptionStr != null) {
            dateInscription = parseDate(dateInscriptionStr);
        }
        return dateInscription;
    }

    public void setDateInscription(LocalDateTime dateInscription) {
        this.dateInscription = dateInscription;
    }

    // ✅ AJOUT: Getters/setters pour les strings de dates (compatibilité JSON)
    public String getDateInscriptionStr() { return dateInscriptionStr; }
    public void setDateInscriptionStr(String dateInscriptionStr) {
        this.dateInscriptionStr = dateInscriptionStr;
        this.dateInscription = null; // Reset pour re-parser
    }

    public String getCreatedAtStr() { return createdAtStr; }
    public void setCreatedAtStr(String createdAtStr) { this.createdAtStr = createdAtStr; }

    public String getUpdatedAtStr() { return updatedAtStr; }
    public void setUpdatedAtStr(String updatedAtStr) { this.updatedAtStr = updatedAtStr; }

    public List<CompteDTO> getComptes() { return comptes; }
    public void setComptes(List<CompteDTO> comptes) { this.comptes = comptes; }

    // ✅ MÉTHODE REQUISE: getNomComplet() utilisée dans votre code
    public String getNomComplet() {
        return nom + " " + prenom;
    }

    // ✅ AJOUT: Méthode pour parser les dates de façon robuste
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            // Essayer d'abord le format avec microsecondes
            return LocalDateTime.parse(dateStr, LARAVEL_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Essayer le format simple
                return LocalDateTime.parse(dateStr, SIMPLE_FORMATTER);
            } catch (DateTimeParseException e2) {
                // Si ça ne marche pas, nettoyer la string et réessayer
                String cleanDate = dateStr.replaceAll("\\.[0-9]+Z$", "Z");
                try {
                    return LocalDateTime.parse(cleanDate, SIMPLE_FORMATTER);
                } catch (DateTimeParseException e3) {
                    System.err.println("⚠️ Impossible de parser la date: " + dateStr);
                    return LocalDateTime.now(); // Fallback
                }
            }
        }
    }

    // Méthodes utilitaires existantes
    public boolean isActif() {
        return "Actif".equals(statut);
    }

    public boolean isSuspendu() {
        return "Suspendu".equals(statut);
    }

    @Override
    public String toString() {
        return "ClientDTO{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}