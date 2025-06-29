package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ✅ DTO pour la classe Transaction - Version corrigée sans LocalDateTime direct
 */
public class TransactionDTO {
    private Long id;
    private String type; // "Dépôt", "Retrait", "Virement"
    private Double montant;

    // ✅ CHANGEMENT: Gestion de la date comme String pour éviter les erreurs Gson
    private String date; // Format ISO depuis l'API
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    private String statut; // "Validé", "Rejeté", "En attente"
    private String description;

    @SerializedName("compte_source_id")
    private Long compteSourceId;

    @SerializedName("compte_dest_id")
    private Long compteDestId;

    // Références aux comptes (pour les jointures)
    private CompteDTO compteSource;
    private CompteDTO compteDestination;

    // ✅ Formatters pour les dates
    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"), // Laravel avec microsecondes
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),        // ISO standard
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")              // Format simple
    };

    // Constructeurs
    public TransactionDTO() {}

    public TransactionDTO(String type, Double montant, Long compteSourceId) {
        this.type = type;
        this.montant = montant;
        this.compteSourceId = compteSourceId;
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.statut = "En attente";
    }

    public TransactionDTO(String type, Double montant, Long compteSourceId, String description) {
        this(type, montant, compteSourceId);
        this.description = description;
    }

    // ✅ GETTERS ET SETTERS STANDARD
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getMontant() {
        return montant != null ? montant : 0.0;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    // ✅ GESTION DE DATE COMME STRING
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ✅ MÉTHODES POUR CONVERTIR EN LocalDateTime SEULEMENT QUAND NÉCESSAIRE
    public LocalDateTime getDateAsLocalDateTime() {
        return parseDate(date);
    }

    public LocalDateTime getCreatedAtAsLocalDateTime() {
        return parseDate(createdAt);
    }

    public LocalDateTime getUpdatedAtAsLocalDateTime() {
        return parseDate(updatedAt);
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

        // Si aucun format ne marche, essayer de nettoyer
        try {
            String cleanDate = dateStr
                    .replaceAll("\\.[0-9]+Z$", "Z")  // Enlever les microsecondes
                    .replaceAll("Z$", "");           // Enlever le Z

            return LocalDateTime.parse(cleanDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (DateTimeParseException e) {
            System.err.println("⚠️ Impossible de parser la date: " + dateStr + " - Utilisation de la date actuelle");
            return LocalDateTime.now(); // Fallback
        }
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCompteSourceId() {
        return compteSourceId;
    }

    public void setCompteSourceId(Long compteSourceId) {
        this.compteSourceId = compteSourceId;
    }

    public Long getCompteDestId() {
        return compteDestId;
    }

    public void setCompteDestId(Long compteDestId) {
        this.compteDestId = compteDestId;
    }

    public CompteDTO getCompteSource() {
        return compteSource;
    }

    public void setCompteSource(CompteDTO compteSource) {
        this.compteSource = compteSource;
    }

    public CompteDTO getCompteDestination() {
        return compteDestination;
    }

    public void setCompteDestination(CompteDTO compteDestination) {
        this.compteDestination = compteDestination;
    }

    // ✅ MÉTHODES UTILITAIRES
    public boolean isValidee() {
        return "Validé".equals(statut);
    }

    public boolean isRejetee() {
        return "Rejeté".equals(statut);
    }

    public boolean isEnAttente() {
        return "En attente".equals(statut);
    }

    public boolean isDebit() {
        return "Retrait".equals(type) || "Virement".equals(type);
    }

    public boolean isCredit() {
        return "Dépôt".equals(type);
    }

    public boolean isVirement() {
        return "Virement".equals(type);
    }

    public String getMontantFormate() {
        return String.format("%.2f FCFA", getMontant());
    }

    public String getDateFormatee() {
        LocalDateTime dateTime = getDateAsLocalDateTime();
        if (dateTime != null) {
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return date != null ? date.substring(0, Math.min(10, date.length())) : "";
    }

    public String getTypeIcon() {
        return switch (type != null ? type : "") {
            case "Dépôt" -> "💰";
            case "Retrait" -> "💸";
            case "Virement" -> "🔄";
            default -> "💳";
        };
    }

    public String getStatutColor() {
        return switch (statut != null ? statut : "") {
            case "Validé" -> "#27ae60";
            case "Rejeté" -> "#e74c3c";
            case "En attente" -> "#f39c12";
            default -> "#95a5a6";
        };
    }

    public String getCompteSourceNumero() {
        return compteSource != null ? compteSource.getNumero() : "";
    }

    public String getCompteDestNumero() {
        return compteDestination != null ? compteDestination.getNumero() : "";
    }

    @Override
    public String toString() {
        return "TransactionDTO{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", montant=" + montant +
                ", date='" + date + '\'' +
                ", statut='" + statut + '\'' +
                ", compteSourceId=" + compteSourceId +
                ", compteDestId=" + compteDestId +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TransactionDTO that = (TransactionDTO) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}