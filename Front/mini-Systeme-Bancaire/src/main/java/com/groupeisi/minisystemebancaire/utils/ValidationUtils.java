package com.groupeisi.minisystemebancaire.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{9}$"
    );

    private static final Pattern IBAN_PATTERN = Pattern.compile(
            "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$"
    );

    private static final Pattern CODE_PIN_PATTERN = Pattern.compile(
            "^[0-9]{4}$"
    );

    /**
     * Vérifie si une chaîne n'est pas vide ou nulle
     * @param value la chaîne à vérifier
     * @return true si la chaîne n'est pas null et n'est pas vide (après suppression des espaces)
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Vérifie si une chaîne est vide ou nulle
     * @param value la chaîne à vérifier
     * @return true si la chaîne est null ou vide (après suppression des espaces)
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidIban(String iban) {
        return iban != null && IBAN_PATTERN.matcher(iban).matches();
    }

    public static boolean isValidCodePin(String pin) {
        return pin != null && CODE_PIN_PATTERN.matcher(pin).matches();
    }

    public static boolean isValidAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return false;
        }
        try {
            BigDecimal value = new BigDecimal(amount);
            return value.compareTo(BigDecimal.ZERO) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isAdult(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }
        return birthDate.plusYears(18).isBefore(LocalDate.now()) ||
                birthDate.plusYears(18).isEqual(LocalDate.now());
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            else if (Character.isLowerCase(c)) hasLowercase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }

    /**
     * Validation simple du mot de passe (minimum 6 caractères)
     * @param password le mot de passe à vérifier
     * @return true si le mot de passe a au moins 6 caractères
     */
    public static boolean isValidSimplePassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.replaceAll("\\s", "").length() != 16) {
            return false;
        }

        // Algorithme de Luhn pour la validation du numéro de carte
        String number = cardNumber.replaceAll("\\s", "");
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    public static boolean isValidCVV(String cvv) {
        return cvv != null && cvv.matches("^[0-9]{3,4}$");
    }

    public static boolean isValidExpiryDate(String date) {
        if (date == null || !date.matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
            return false;
        }

        String[] parts = date.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000;

        LocalDate expiryDate = LocalDate.of(year, month, 1)
                .plusMonths(1).minusDays(1);

        return !expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Valide la longueur minimale d'une chaîne
     * @param value la chaîne à vérifier
     * @param minLength longueur minimale requise
     * @return true si la chaîne a au moins la longueur minimale
     */
    public static boolean hasMinLength(String value, int minLength) {
        return value != null && value.length() >= minLength;
    }

    /**
     * Valide la longueur maximale d'une chaîne
     * @param value la chaîne à vérifier
     * @param maxLength longueur maximale autorisée
     * @return true si la chaîne n'excède pas la longueur maximale
     */
    public static boolean hasMaxLength(String value, int maxLength) {
        return value == null || value.length() <= maxLength;
    }

    /**
     * Valide qu'une chaîne contient uniquement des lettres et des espaces
     * @param value la chaîne à vérifier
     * @return true si la chaîne ne contient que des lettres et des espaces
     */
    public static boolean isValidName(String value) {
        return value != null && value.matches("^[a-zA-ZÀ-ÿ\\s]+$");
    }

    /**
     * Valide qu'un montant est dans une plage donnée
     * @param amount le montant sous forme de chaîne
     * @param min montant minimum
     * @param max montant maximum
     * @return true si le montant est valide et dans la plage
     */
    public static boolean isAmountInRange(String amount, BigDecimal min, BigDecimal max) {
        if (!isValidAmount(amount)) {
            return false;
        }

        try {
            BigDecimal value = new BigDecimal(amount);
            return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}