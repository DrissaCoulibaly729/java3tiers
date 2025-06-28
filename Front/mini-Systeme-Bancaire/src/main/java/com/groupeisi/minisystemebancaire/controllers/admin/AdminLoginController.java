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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AdminLoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnRetour;
    @FXML private Label lblMessage;

    private final AdminService adminService = new AdminService();

    @FXML
    private void initialize() {
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

        System.out.println("🚀 =========================");
        System.out.println("🚀 DEBUG CONNEXION ADMIN");
        System.out.println("🚀 Username saisi: '" + username + "'");
        System.out.println("🚀 Password saisi: '" + password + "'");
        System.out.println("🚀 =========================");

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Veuillez remplir tous les champs", "error");
            return;
        }

        // Désactiver le bouton pendant la connexion
        btnLogin.setDisable(true);
        btnLogin.setText("Connexion...");

        // Test direct de l'API
        testDirectAPI(username, password);

        // Exécuter la connexion dans un thread séparé
        Thread loginThread = new Thread(() -> {
            try {
                System.out.println("🚀 AVANT appel adminService.login()");
                AdminDTO admin = adminService.login(username, password);
                System.out.println("🚀 APRES appel adminService.login() - Résultat: " + admin);

                Platform.runLater(() -> {
                    if (admin != null) {
                        System.out.println("✅ Connexion admin réussie pour: " + admin.getUsername());

                        SessionManager.setCurrentAdmin(admin);

                        showMessage("Connexion réussie ! Redirection...", "success");

                        Platform.runLater(() -> {
                            try {
                                Thread.sleep(1000);
                                redirectToDashboard(event != null ? event : new ActionEvent(btnLogin, null));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    } else {
                        System.out.println("❌ adminService.login() a retourné null");
                        showMessage("Identifiants incorrects", "error");
                        resetLoginButton();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("❌ Exception dans adminService.login(): " + e.getMessage());
                    e.printStackTrace();

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

    // Test direct de l'API pour comparaison
    private void testDirectAPI(String username, String password) {
        Thread testThread = new Thread(() -> {
            try {
                System.out.println("🧪 =========================");
                System.out.println("🧪 TEST DIRECT API");

                String json = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
                System.out.println("🧪 JSON envoyé: " + json);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8000/api/admins/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = HttpClient.newHttpClient()
                        .send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("🧪 Status direct: " + response.statusCode());
                System.out.println("🧪 Réponse directe: " + response.body());
                System.out.println("🧪 =========================");

            } catch (Exception e) {
                System.out.println("🧪 Erreur test direct: " + e.getMessage());
                e.printStackTrace();
            }
        });

        testThread.setDaemon(true);
        testThread.start();
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