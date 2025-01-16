package com.example.gameshop.utils;

public class InputValidator {
    public static boolean isValidUsername(String username) {
        // At least 3 characters, alphanumeric only
        return username != null && username.matches("^[a-zA-Z0-9]{3,20}$");
    }

    public static boolean isValidPassword(String password) {
        // At least 8 characters, must contain letters and numbers
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidPrice(String price) {
        try {
            double value = Double.parseDouble(price);
            return value >= 0 && value <= 1000; // Reasonable price range
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidGameTitle(String title) {
        return title != null && title.matches("^[a-zA-Z0-9\\s\\-:]{3,50}$");
    }

    public static String sanitizeInput(String input) {
        if (input == null) return "";
        // Remove any HTML or SQL injection attempts
        return input.replaceAll("[<>'\";]", "");
    }
} 