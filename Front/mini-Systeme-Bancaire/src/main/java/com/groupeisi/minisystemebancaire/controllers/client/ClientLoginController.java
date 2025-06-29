package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.ClientService;
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
import java.util.regex.Pattern;

public class ClientLoginController {
    private final ClientService clientService = new ClientService();

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button btnQuitter;
    @FXML private Button btnInscription;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        // Configuration initiale
        lblMessage.setText("");
        setupEventHandlers();
        setupEnterKeyHandler();
    }

    private void setupEventHandlers() {
        btnLogin.setOnAction(this::handleLogin);
        btnQuitter.setOnAction(this::handleQuitter);
        if (btnInscription != null) {
            btnInscription.setOnAction(this::handleInscription);
        }
    }

    private void setupEnterKeyHandler() {
        txtPassword.setOnAction(this::handleLogin);
        btnLogin.setDefaultButton(true);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("Veuillez remplir tous les champs", "error");
            return;
        }

        // Validation du format email
        if (!isValidEmail(email)) {
            showMessage("Format d'email invalide", "error");
            return;
        }

        // Désactiver le bouton pendant la connexion
        btnLogin.setDisable(true);
        btnLogin.setText("Connexion...");

        // Exécuter la connexion dans un thread séparé pour éviter le blocage de l'UI
        Thread loginThread = new Thread(() -> {
            try {
                System.out.println("🔐 Tentative de connexion client...");
                ClientDTO client = clientService.login(email, password);

                Platform.runLater(() -> {
                    if (client != null && client.getId() != null) {
                        System.out.println("✅ Connexion réussie pour: " + client.getEmail() + " (ID: " + client.getId() + ")");

                        // ✅ CORRECTION : Vérifier que l'ID existe avant de sauvegarder la session
                        SessionManager.setCurrentClient(client);

                        showMessage("Connexion réussie !", "success");

                        // Redirection vers le dashboard client
                        navigateToClientDashboard();
                    } else {
                        System.err.println("❌ Connexion échouée : client null ou ID manquant");
                        showMessage("Email ou mot de passe incorrect", "error");
                        resetLoginButton();
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la connexion: " + e.getMessage());
                Platform.runLater(() -> {
                    showMessage("Erreur de connexion: " + e.getMessage(), "error");
                    resetLoginButton();
                });
            }
        });

        loginThread.setDaemon(true);
        loginThread.start();
    }

    @FXML
    private void handleQuitter(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void handleInscription(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/UI_Register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur lors de l'ouverture de la page d'inscription", "error");
        }
    }

    // ✅ AJOUT : Méthode handleRegister pour correspondre au FXML
    @FXML
    private void handleRegister(ActionEvent event) {
        handleInscription(event);
    }

    // ✅ MÉTHODES UTILITAIRES

    private void navigateToClientDashboard() {
        try {
            System.out.println("🚀 Navigation vers le dashboard client...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/UI_Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Erreur lors de la redirection", "error");
            resetLoginButton();
        }
    }

    private void resetLoginButton() {
        btnLogin.setDisable(false);
        btnLogin.setText("Se connecter");
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private void showMessage(String message, String type) {
        if (lblMessage != null) {
            lblMessage.setText(message);
            lblMessage.setStyle(
                    "error".equals(type) ? "-fx-text-fill: red;" :
                            "success".equals(type) ? "-fx-text-fill: green;" :
                                    "-fx-text-fill: blue;"
            );
        }
        System.out.println(("error".equals(type) ? "❌ " : "✅ ") + message);
    }
}