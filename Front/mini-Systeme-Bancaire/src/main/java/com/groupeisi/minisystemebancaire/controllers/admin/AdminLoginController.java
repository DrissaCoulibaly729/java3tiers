package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.AdminDTO;
import com.groupeisi.minisystemebancaire.services.AdminService;
import com.groupeisi.minisystemebancaire.utils.SessionManager;
import javafx.application.Platform;
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
        txtPassword.setOnAction(this::handleLogin);
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

        // Désactiver le bouton pendant la connexion
        btnLogin.setDisable(true);
        btnLogin.setText("Connexion...");

        // Exécuter la connexion dans un thread séparé
        Thread loginThread = new Thread(() -> {
            try {
                System.out.println("🔐 Tentative de connexion admin...");
                AdminDTO admin = adminService.login(username, password);

                Platform.runLater(() -> {
                    if (admin != null) {
                        System.out.println("✅ Connexion admin réussie pour: " + admin.getUsername());

                        // Stocker les informations de session
                        SessionManager.setCurrentAdmin(admin);

                        showMessage("Connexion réussie ! Redirection...", "success");

                        // Redirection vers le dashboard admin après un court délai
                        Platform.runLater(() -> {
                            try {
                                Thread.sleep(1000);
                                redirectToDashboard(event != null ? event : new ActionEvent(btnLogin, null));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    } else {
                        showMessage("Identifiants incorrects", "error");
                        resetLoginButton();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("❌ Identifiants admin incorrects");

                    String errorMessage = e.getMessage();
                    if (errorMessage.contains("serveur")) {
                        showMessage("Erreur de connexion au serveur. Vérifiez votre connexion.", "error");
                    } else {
                        showMessage("Identifiants incorrects", "error");
                    }

                    txtPassword.clear();
                    resetLoginButton();
                });
            }
        });

        loginThread.setDaemon(true);
        loginThread.start();
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
            System.out.println("🚀 Navigation vers : " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur lors de la navigation vers " + fxmlPath, "error");
            resetLoginButton();
        }
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.setStyle(type.equals("error") ?
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" :
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void resetLoginButton() {
        btnLogin.setDisable(false);
        btnLogin.setText("Se connecter");
    }
}