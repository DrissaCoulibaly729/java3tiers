package com.groupeisi.minisystemebancaire.utils;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ✅ CORRIGÉ : Adaptateur pour gérer les différents formats de date Laravel
 */
public final class LocalDateTimeTypeAdapter
        implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    // Format Laravel API (avec timezone Z et microsecondes)
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

    // Format de sauvegarde simple
    private static final DateTimeFormatter SIMPLE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Format alternatif sans microsecondes
    private static final DateTimeFormatter ISO_SIMPLE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public JsonElement serialize(LocalDateTime src,
                                 Type typeOfSrc,
                                 JsonSerializationContext ctx) {
        // Utilise le format simple pour l'envoi vers Laravel
        return new JsonPrimitive(src.format(SIMPLE_FORMATTER));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json,
                                     Type typeOfT,
                                     JsonDeserializationContext ctx)
            throws JsonParseException {

        String dateString = json.getAsString();

        try {
            // Essai 1: Format Laravel avec microsecondes et timezone Z
            if (dateString.contains("T") && dateString.endsWith("Z")) {
                if (dateString.contains(".")) {
                    // Format: 2025-06-28T00:41:58.000000Z
                    return LocalDateTime.parse(dateString, ISO_FORMATTER);
                } else {
                    // Format: 2025-06-28T00:41:58Z
                    return LocalDateTime.parse(dateString, ISO_SIMPLE);
                }
            }

            // Essai 2: Format simple
            return LocalDateTime.parse(dateString, SIMPLE_FORMATTER);

        } catch (DateTimeParseException e) {
            System.err.println("❌ Erreur parsing date: " + dateString);
            System.err.println("❌ Formats supportés:");
            System.err.println("   - yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z' (Laravel)");
            System.err.println("   - yyyy-MM-dd'T'HH:mm:ss'Z' (Laravel simple)");
            System.err.println("   - yyyy-MM-dd HH:mm:ss (Simple)");

            throw new JsonParseException("Impossible de parser la date: " + dateString, e);
        }
    }
}