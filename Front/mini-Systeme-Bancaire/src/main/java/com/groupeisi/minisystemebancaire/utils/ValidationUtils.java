package com.groupeisi.minisystemebancaire.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+221|221|0)?[0-9]{9}$"
    );

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone.replaceAll("\\s", "")).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static String formatPhone(String phone) {
        if (phone == null) return "";

        // Supprimer tous les espaces et caractères non numériques
        String cleaned = phone.replaceAll("[^0-9+]", "");

        // Ajouter le préfixe sénégalais si nécessaire
        if (cleaned.length() == 9) {
            cleaned = "+221" + cleaned;
        } else if (cleaned.startsWith("0") && cleaned.length() == 10) {
            cleaned = "+221" + cleaned.substring(1);
        } else if (cleaned.startsWith("221") && cleaned.length() == 12) {
            cleaned = "+" + cleaned;
        }

        return cleaned;
    }

    public static String formatAmount(double amount) {
        return String.format("%.2f €", amount);
    }
}
