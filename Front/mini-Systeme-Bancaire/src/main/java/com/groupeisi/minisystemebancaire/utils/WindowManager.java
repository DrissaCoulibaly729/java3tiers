package com.groupeisi.minisystemebancaire.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class WindowManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void openWindow(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    public static void closeWindow() {
        if (primaryStage != null) {
            primaryStage.close();
        }
    }

    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showSuccess(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Affiche un popup de confirmation avec Oui/Non
     * @param title Titre de la fenêtre
     * @param header En-tête du message
     * @param content Contenu du message
     * @return true si l'utilisateur a cliqué OK, false sinon
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
     * @param message Message à afficher
     * @return true si l'utilisateur a cliqué OK, false sinon
     */
    public static boolean showConfirmation(String message) {
        return showConfirmation("Confirmation", "Confirmer l'action", message);
    }

    /**
     * Affiche un popup d'information simple
     * @param message Message à afficher
     */
    public static void showInfo(String message) {
        showSuccess("Information", null, message);
    }

    /**
     * Affiche un popup d'erreur simple
     * @param message Message d'erreur à afficher
     */
    public static void showError(String message) {
        showError("Erreur", null, message);
    }

    /**
     * Affiche un popup d'avertissement simple
     * @param message Message d'avertissement à afficher
     */
    public static void showWarning(String message) {
        showWarning("Avertissement", null, message);
    }
}