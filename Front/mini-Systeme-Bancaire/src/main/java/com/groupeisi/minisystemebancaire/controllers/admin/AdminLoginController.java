package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.services.AdminService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminLoginController {

    private final AdminService adminService = new AdminService();

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnQuitter;

    @FXML
    public void initialize() {
        btnLogin.setOnAction(event -> handleLogin());
        btnQuitter.setOnAction(event -> handleQuitter());
    }

    /**
     * ✅ Gère la connexion de l'admin
     */
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer votre nom d'utilisateur et votre mot de passe.");
            return;
        }

        AdminDTO admin = adminService.authentifierAdmin(username, password);

        if (admin != null) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Connexion réussie !");
            // 🚀 Rediriger vers le dashboard admin
            ouvrirDashboard();
        } else {
            showAlert(Alert.AlertType.ERROR, "Échec", "Nom d'utilisateur ou mot de passe incorrect.");
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
     * ✅ Ouvre le tableau de bord de l'admin
     */
    private void ouvrirDashboard() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/admin/UI_Dashboard.fxml"));
            javafx.scene.Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Tableau de Bord Admin");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le tableau de bord.");
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
