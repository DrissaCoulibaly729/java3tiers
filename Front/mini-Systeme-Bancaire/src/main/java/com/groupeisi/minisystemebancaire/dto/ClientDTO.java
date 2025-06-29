package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * ✅ DTO pour la classe Client - Version corrigée pour les dates Laravel
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

    // ✅ CHANGEMENT: Gestion des dates comme String pour éviter les erreurs de parsing
    @SerializedName("date_inscription")
    private String dateInscriptionStr;

    @SerializedName("created_at")
    private String createdAtStr;

    @SerializedName("updated_at")
    private String updatedAtStr;

    private List<CompteDTO> comptes;

    // ✅ Formatters pour les dates Laravel
    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"), // Laravel avec microsecondes
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),        // ISO standard
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")              // Format simple
    };

    // Constructeurs
    public ClientDTO() {}

    public ClientDTO(String nom, String prenom, String email, String telephone, String adresse, String password) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.password = password;
        this.statut = "Actif";
    }

    // ✅ GETTERS ET SETTERS STANDARD
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

    // ✅ GESTION DES DATES COMME STRING (pas de parsing automatique)
    public String getDateInscriptionStr() { return dateInscriptionStr; }
    public void setDateInscriptionStr(String dateInscriptionStr) { this.dateInscriptionStr = dateInscriptionStr; }

    public String getCreatedAtStr() { return createdAtStr; }
    public void setCreatedAtStr(String createdAtStr) { this.createdAtStr = createdAtStr; }

    public String getUpdatedAtStr() { return updatedAtStr; }
    public void setUpdatedAtStr(String updatedAtStr) { this.updatedAtStr = updatedAtStr; }

    public List<CompteDTO> getComptes() { return comptes; }
    public void setComptes(List<CompteDTO> comptes) { this.comptes = comptes; }

    // ✅ MÉTHODES REQUISES PAR VOTRE CODE
    public String getNomComplet() {
        return (prenom != null ? prenom : "") + " " + (nom != null ? nom : "");
    }

    // ✅ MÉTHODES POUR PARSER LES DATES SEULEMENT QUAND NÉCESSAIRE
    public LocalDateTime getDateInscription() {
        return parseDate(dateInscriptionStr);
    }

    public LocalDateTime getCreatedAt() {
        return parseDate(createdAtStr);
    }

    public LocalDateTime getUpdatedAt() {
        return parseDate(updatedAtStr);
    }

    // ✅ MÉTHODE POUR FORMATER LES DATES POUR L'AFFICHAGE
    public String getDateInscriptionFormatee() {
        LocalDateTime date = getDateInscription();
        if (date != null) {
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        }
        return dateInscriptionStr != null ? dateInscriptionStr.substring(0, 10) : "";
    }

    // ✅ PARSING ROBUSTE DES DATES
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        // Essayer chaque format
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {
                // Continuer avec le format suivant
            }
        }

        // Si aucun format ne marche, extraire juste la partie date/heure
        try {
            String cleanDate = dateStr
                    .replaceAll("\\.[0-9]+Z$", "Z")  // Enlever les microsecondes
                    .replaceAll("Z$", "");           // Enlever le Z

            return LocalDateTime.parse(cleanDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (DateTimeParseException e) {
            System.err.println("⚠️ Impossible de parser la date: " + dateStr + " - Utilisation de la date actuelle");
            return LocalDateTime.now(); // Fallback pour éviter les crashes
        }
    }

    // ✅ MÉTHODES UTILITAIRES
    public boolean isActif() {
        return "Actif".equals(statut);
    }

    public boolean isSuspendu() {
        return "Suspendu".equals(statut);
    }

    public boolean isFerme() {
        return "Fermé".equals(statut);
    }

    // ✅ MÉTHODES POUR LES STATISTIQUES
    public int getNombreComptes() {
        return comptes != null ? comptes.size() : 0;
    }

    public double getSoldeTotal() {
        if (comptes == null) return 0.0;
        return comptes.stream()
                .filter(c -> "Actif".equals(c.getStatut()))
                .mapToDouble(c -> c.getSolde() != null ? c.getSolde() : 0.0)
                .sum();
    }

    @Override
    public String toString() {
        return "ClientDTO{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", statut='" + statut + '\'' +
                ", comptes=" + (comptes != null ? comptes.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientDTO clientDTO = (ClientDTO) o;
        return id != null && id.equals(clientDTO.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}