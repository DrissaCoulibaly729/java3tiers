package com.groupeisi.minisystemebancaire.controllers;

import com.groupeisi.minisystemebancaire.services.ApiService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private Button btnAdmin;
    @FXML private Button btnClient;
    @FXML private Label lblStatus;

    @FXML
    public void initialize() {
        // Vérifier la connexion au backend au démarrage
        checkBackendConnection();
    }

    @FXML
    public void handleAdminClick(ActionEvent event) {
        System.out.println("🔐 Redirection vers connexion admin...");
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Login.fxml", event);
    }

    @FXML
    public void handleClientClick(ActionEvent event) {
        System.out.println("👤 Redirection vers connexion client...");
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Login.fxml", event);
    }

    private void checkBackendConnection() {
        // Vérifier la connexion dans un thread séparé
        Thread connectionThread = new Thread(() -> {
            boolean isConnected = ApiService.testConnection();

            Platform.runLater(() -> {
                if (lblStatus != null) {
                    if (isConnected) {
                        lblStatus.setText("✅ Connexion au serveur établie");
                        lblStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
                    } else {
                        lblStatus.setText("❌ Serveur indisponible - Vérifiez que Laravel est démarré");
                        lblStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

                        // Afficher une alerte pour informer l'utilisateur
                        showConnectionAlert();
                    }
                }
            });
        });

        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void showConnectionAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Problème de connexion");
        alert.setHeaderText("Serveur backend indisponible");
        alert.setContentText("Le serveur Laravel n'est pas accessible.\n\n" +
                "Vérifiez que :\n" +
                "• Le serveur Laravel est démarré (php artisan serve)\n" +
                "• Il fonctionne sur http://localhost:8000\n" +
                "• Votre base de données est configurée\n\n" +
                "L'application peut ne pas fonctionner correctement.");

        alert.showAndWait();
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            System.out.println("🚀 Navigation vers : " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible de charger la page");
            alert.setContentText("Erreur lors du chargement de : " + fxmlPath + "\n\n" + e.getMessage());
            alert.showAndWait();
        }
    }
}