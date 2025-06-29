package com.groupeisi.minisystemebancaire.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtil {

    // Formatter pour les dates avec microsecondes (format Laravel)
    private static final DateTimeFormatter LARAVEL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

    // Formatter pour les dates ISO standard
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // Formatter pour affichage
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Parse une date string en LocalDateTime
     * Gère les formats Laravel avec microsecondes
     */
    public static LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            // Essayer d'abord le format avec microsecondes
            return LocalDateTime.parse(dateStr, LARAVEL_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Essayer le format ISO standard
                return LocalDateTime.parse(dateStr, ISO_FORMATTER);
            } catch (DateTimeParseException e2) {
                // Si ça ne marche pas, essayer de nettoyer la string
                String cleanDate = dateStr.replaceAll("\\.[0-9]+Z$", "Z");
                return LocalDateTime.parse(cleanDate, ISO_FORMATTER);
            }
        }
    }

    /**
     * Formate une date pour l'affichage
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }

    /**
     * Formate une date pour l'API
     */
    public static String formatForApi(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }
}