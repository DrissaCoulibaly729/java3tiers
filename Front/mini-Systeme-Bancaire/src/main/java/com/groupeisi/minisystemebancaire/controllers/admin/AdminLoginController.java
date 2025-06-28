package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.services.AdminService;
import com.groupeisi.minisystemebancaire.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminLoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnRetour;
    @FXML private Label lblMessage;

    private final AdminService adminService = new AdminService();

    @FXML
    private void initialize() {
        // Configuration initiale
        lblMessage.setText("");
        setupEnterKeyHandler();
    }

    private void setupEnterKeyHandler() {
        txtPassword.setOnAction(e -> handleLogin(null));
        btnLogin.setDefaultButton(true);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Veuillez remplir tous les champs", "error");
            return;
        }

        try {
            AdminDTO admin = adminService.login(username, password);
            SessionManager.setCurrentAdmin(admin);

            showMessage("Connexion réussie !", "success");

            // Redirection vers le dashboard admin
            redirectToDashboard(event != null ? event :
                    new ActionEvent(btnLogin, null));

        } catch (Exception e) {
            showMessage("Identifiants incorrects", "error");
            txtPassword.clear();
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/UI_Main.fxml", event);
    }

    private void redirectToDashboard(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/admin/UI_Dashboard.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur lors de la navigation", "error");
        }
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.setStyle(type.equals("error") ?
                "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
