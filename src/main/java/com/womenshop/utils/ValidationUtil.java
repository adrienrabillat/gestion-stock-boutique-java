package com.womenshop.utils;

import javafx.scene.control.Alert;

public class ValidationUtil {

    // Vérifie si une chaîne est vide ou nulle
    public static boolean isStringEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    // Vérifie si c'est un nombre décimal positif (ex: Prix)
    public static boolean isPositiveDouble(String text) {
        try {
            double d = Double.parseDouble(text);
            return d >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Vérifie si c'est un nombre entier positif (ex: Stock)
    public static boolean isPositiveInt(String text) {
        try {
            int i = Integer.parseInt(text);
            return i >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Affiche une alerte d'erreur standardisée
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}