package com.example.gameshop.utils;

import java.util.logging.Logger;

public class DatabaseValidator {
    private static final Logger logger = Logger.getLogger(DatabaseValidator.class.getName());
    
    public static String validateString(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            logger.warning(fieldName + " is null or empty, using default value");
            return "Unknown " + fieldName;
        }
        
        // Trim and limit length
        value = value.trim();
        if (value.length() > maxLength) {
            logger.warning(fieldName + " exceeds maximum length, truncating");
            return value.substring(0, maxLength);
        }
        
        return value;
    }
    
    public static double validatePrice(Double price) {
        if (price == null || price < 0) {
            logger.warning("Invalid price value, using default");
            return 0.0;
        }

        return Math.round(price * 100.0) / 100.0;
    }
    
    public static int validateInteger(Integer value, String fieldName) {
        if (value == null || value < 0) {
            logger.warning("Invalid " + fieldName + " value, using default");
            return 0;
        }
        return value;
    }
} 