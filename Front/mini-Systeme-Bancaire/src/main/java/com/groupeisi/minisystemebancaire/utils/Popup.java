package com.groupeisi.minisystemebancaire.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utilitaire pour afficher des popups et dialogues avec JavaFX
 */
public class Popup {

    /**
     * Affiche un popup d'information
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Affiche un popup d'information simple
     */
    public static void showInfo(String message) {
        showInfo("Information", "Information", message);
    }

    /**
     * Affiche un popup d'erreur
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Affiche un popup d'erreur simple
     */
    public static void showError(String message) {
        showError("Erreur", "Une erreur s'est produite", message);
    }

    /**
     * Affiche un popup d'avertissement
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Affiche un popup d'avertissement simple
     */
    public static void showWarning(String message) {
        showWarning("Attention", "Attention", message);
    }

    /**
     * Affiche un popup de succès
     */
    public static void showSuccess(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Personnaliser l'icône pour le succès si nécessaire
        alert.showAndWait();
    }

    /**
     * Affiche un popup de succès simple
     */
    public static void showSuccess(String message) {
        showSuccess("Succès", "Opération réussie", message);
    }

    /**
     * Affiche un popup de confirmation avec Oui/Non
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Affiche un popup de confirmation simple
     */
    public static boolean showConfirmation(String message) {
        return showConfirmation("Confirmation", "Confirmer l'action", message);
    }

    /**
     * Affiche un dialogue de saisie de texte
     */
    public static Optional<String> showTextInput(String title, String header, String content, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        return dialog.showAndWait();
    }

    /**
     * Affiche un dialogue de saisie de texte simple
     */
    public static Optional<String> showTextInput(String message) {
        return showTextInput("Saisie", "Saisie de texte", message, "");
    }

    /**
     * Affiche un dialogue de saisie de texte avec valeur par défaut
     */
    public static Optional<String> showTextInput(String message, String defaultValue) {
        return showTextInput("Saisie", "Saisie de texte", message, defaultValue);
    }

    /**
     * Affiche un popup personnalisé avec type spécifique
     */
    public static void showCustom(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Configure le style des alertes (optionnel)
     */
    public static void setAlertStyle(Alert alert, String cssClass) {
        if (alert.getDialogPane().getScene() != null) {
            alert.getDialogPane().getStyleClass().add(cssClass);
        }
    }

    /**
     * Ferme toutes les alertes ouvertes
     */
    public static void closeAllAlerts() {
        // Cette méthode peut être étendue si nécessaire
        // pour gérer plusieurs alertes simultanées
    }

    /**
     * Affiche une notification rapide (peut être étendue avec des bibliothèques tierces)
     */
    public static void showNotification(String title, String message) {
        // Pour l'instant, utilise une alerte simple
        // Peut être remplacée par une vraie notification système
        showInfo(title, null, message);
    }

    /**
     * Affiche un popup d'aide
     */
    public static void showHelp(String content) {
        showInfo("Aide", "Information d'aide", content);
    }

    /**
     * Affiche un popup "À propos"
     */
    public static void showAbout(String appName, String version, String description) {
        String content = String.format(
                "Application : %s\nVersion : %s\n\n%s",
                appName, version, description
        );
        showInfo("À propos", "À propos de l'application", content);
    }
}