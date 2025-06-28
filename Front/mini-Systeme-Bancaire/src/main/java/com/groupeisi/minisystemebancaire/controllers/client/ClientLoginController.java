package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.ClientService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class ClientLoginController {
    private final ClientService clientService = new ClientService();

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin, btnQuitter;

    @FXML
    public void initialize() {
        btnLogin.setOnAction(event -> handleLogin());
        btnQuitter.setOnAction(event -> handleQuitter());
    }

    /**
     * ✅ Gère la connexion du client
     */
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Veuillez remplir tous les champs !");
            return;
        }

        try {
            // Authentifier le client
            ClientDTO client = clientService.login(email, password);

            if (client != null) {
                showAlert(Alert.AlertType.INFORMATION, "Connexion réussie", "Bienvenue, " + client.getNom() + " !");
                ouvrirDashboard(client.getId());  // ✅ On passe l'ID du client ici
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec de connexion", "Email ou mot de passe incorrect.");
            }
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", e.getMessage());
        }
    }

    /**
     * ✅ Ferme l'application
     */
    private void handleQuitter() {
        Stage stage = (Stage) btnQuitter.getScene().getWindow();
        stage.close();
    }

    /**
     * ✅ Ouvre le tableau de bord client avec l'ID du client
     */
    private void ouvrirDashboard(Long clientId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/UI_Dashboard.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et passer l'ID du client
            ClientDashboardController dashboardController = loader.getController();
            dashboardController.setClientId(clientId);  // ✅ CORRECTION : utiliser setClientId au lieu de loadDashboard

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de Bord Client");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le tableau de bord : " + e.getMessage());
        }
    }

    /**
     * ✅ Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
