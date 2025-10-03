package com.bobobo.plugins.b0nuscode.ut;

public class ValidationUtils {

    public static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isValidPromoCode(String promoCode) {
        if (!isValidString(promoCode)) {
            return false;
        }

        return promoCode.matches("^[a-zA-Z0-9-_]+$");
    }

    public static String normalizePromoCode(String promoCode) {
        if (!isValidString(promoCode)) {
            return "";
        }
        return promoCode.toLowerCase().trim();
    }
    public static boolean isPositiveNumber(long number) {
        return number > 0;
    }

    public static boolean isNonNegativeNumber(long number) {
        return number >= 0;
    }
}
