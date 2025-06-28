package com.groupeisi.minisystemebancaire.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {
    private static final DecimalFormat CURRENCY_FORMAT;
    private static final DecimalFormat COMPACT_FORMAT;
    private static final DecimalFormat DECIMAL_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);

        CURRENCY_FORMAT = new DecimalFormat("#,##0.00 CFA", symbols);
        CURRENCY_FORMAT.setGroupingUsed(true);
        CURRENCY_FORMAT.setMinimumFractionDigits(2);
        CURRENCY_FORMAT.setMaximumFractionDigits(2);

        COMPACT_FORMAT = new DecimalFormat("#,##0.## CFA", symbols);
        COMPACT_FORMAT.setGroupingUsed(true);

        DECIMAL_FORMAT = new DecimalFormat("#,##0.00", symbols);
        DECIMAL_FORMAT.setGroupingUsed(true);
    }

    public static String format(BigDecimal amount) {
        if (amount == null) return "0,00 CFA";
        return CURRENCY_FORMAT.format(amount);
    }

    public static String format(Double amount) {
        if (amount == null) return "0,00 CFA";
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatCompact(BigDecimal amount) {
        if (amount == null) return "0 CFA";
        return COMPACT_FORMAT.format(amount);
    }

    public static String formatWithoutCurrency(BigDecimal amount) {
        if (amount == null) return "0,00";
        return DECIMAL_FORMAT.format(amount);
    }

    public static String formatLarge(BigDecimal amount) {
        if (amount == null) return "0 CFA";

        if (amount.compareTo(new BigDecimal("1000000000")) >= 0) {
            return COMPACT_FORMAT.format(amount.divide(new BigDecimal("1000000000"))) + " Mrd";
        } else if (amount.compareTo(new BigDecimal("1000000")) >= 0) {
            return COMPACT_FORMAT.format(amount.divide(new BigDecimal("1000000"))) + " M";
        } else if (amount.compareTo(new BigDecimal("1000")) >= 0) {
            return COMPACT_FORMAT.format(amount.divide(new BigDecimal("1000"))) + " k";
        } else {
            return format(amount);
        }
    }

    public static BigDecimal parse(String amount) {
        try {
            String cleanAmount = amount.replace(" ", "")
                                     .replace("CFA", "")
                                     .replace(",", ".")
                                     .trim();
            return new BigDecimal(cleanAmount);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public static String formatTauxInteret(BigDecimal taux) {
        if (taux == null) return "0,00 %";
        return DECIMAL_FORMAT.format(taux) + " %";
    }

    public static String formatPositiveNegative(BigDecimal amount) {
        if (amount == null) return "0,00 CFA";

        String formatted = format(amount.abs());
        return amount.compareTo(BigDecimal.ZERO) >= 0 ?
            "+" + formatted :
            "-" + formatted;
    }
}
