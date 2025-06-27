package com.groupeisi.minisystemebancaire.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * (Dé)sérialise les LocalDateTime au format « yyyy-MM-dd HH:mm:ss »
 * renvoyé / attendu par l’API Laravel.
 */
public final class LocalDateTimeTypeAdapter
        implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public JsonElement serialize(LocalDateTime src,
                                 Type typeOfSrc,
                                 JsonSerializationContext ctx) {
        return new JsonPrimitive(src.format(FMT));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json,
                                     Type typeOfT,
                                     JsonDeserializationContext ctx)
            throws JsonParseException {
        return LocalDateTime.parse(json.getAsString(), FMT);
    }
}
