package com.groupeisi.minisystemebancaire.dto;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.text.NumberFormat;
import java.util.Locale;

public class CompteDTO {
    private Long id;
    private String numero;
    private String type;
    private Double solde;
    private String statut;

    @SerializedName("date_creation")
    private String dateCreationStr; // Gestion comme String pour éviter les erreurs de parsing

    @SerializedName("client_id")
    private Long clientId;

    @SerializedName("created_at")
    private String createdAtStr;

    @SerializedName("updated_at")
    private String updatedAtStr;

    private ClientDTO client;

    // ✅ Formatters pour les dates Laravel
    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"), // Laravel avec microsecondes
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),        // ISO standard
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")              // Format simple
    };

    // Constructeurs
    public CompteDTO() {}

    public CompteDTO(String numero, String type, Double solde, Long clientId) {
        this.numero = numero;
        this.type = type;
        this.solde = solde;
        this.clientId = clientId;
        this.statut = "Actif";
    }

    // ✅ GETTERS ET SETTERS STANDARD
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getSolde() {
        return solde != null ? solde : 0.0;
    }
    public void setSolde(Double solde) { this.solde = solde; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public ClientDTO getClient() { return client; }
    public void setClient(ClientDTO client) { this.client = client; }

    // ✅ GESTION DES DATES COMME STRING
    public String getDateCreationStr() { return dateCreationStr; }
    public void setDateCreationStr(String dateCreationStr) { this.dateCreationStr = dateCreationStr; }

    public String getCreatedAtStr() { return createdAtStr; }
    public void setCreatedAtStr(String createdAtStr) { this.createdAtStr = createdAtStr; }

    public String getUpdatedAtStr() { return updatedAtStr; }
    public void setUpdatedAtStr(String updatedAtStr) { this.updatedAtStr = updatedAtStr; }

    // ✅ MÉTHODES POUR PARSER LES DATES SEULEMENT QUAND NÉCESSAIRE
    public LocalDateTime getDateCreation() {
        return parseDate(dateCreationStr);
    }

    public LocalDateTime getCreatedAt() {
        return parseDate(createdAtStr);
    }

    public LocalDateTime getUpdatedAt() {
        return parseDate(updatedAtStr);
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

    // ✅ MÉTHODES UTILITAIRES
    public String getSoldeFormate() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        return formatter.format(getSolde()).replace("€", "FCFA");
    }

    public String getDateCreationFormatee() {
        LocalDateTime date = getDateCreation();
        if (date != null) {
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return dateCreationStr != null ? dateCreationStr.substring(0, 10) : "";
    }

    public boolean isActif() {
        return "Actif".equals(statut);
    }

    public boolean isFerme() {
        return "Fermé".equals(statut);
    }

    public String getClientNom() {
        return client != null ? client.getNomComplet() : "";
    }

    public String getTypeIcon() {
        return switch (type != null ? type : "") {
            case "Courant" -> "💳";
            case "Épargne" -> "💰";
            default -> "🏦";
        };
    }

    public String getStatutColor() {
        return switch (statut != null ? statut : "") {
            case "Actif" -> "#27ae60";
            case "Fermé" -> "#e74c3c";
            case "Suspendu" -> "#f39c12";
            default -> "#95a5a6";
        };
    }

    @Override
    public String toString() {
        return "CompteDTO{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", type='" + type + '\'' +
                ", solde=" + solde +
                ", statut='" + statut + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompteDTO compteDTO = (CompteDTO) o;
        return id != null && id.equals(compteDTO.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}