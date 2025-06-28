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
                    if (client != null) {
                        System.out.println("✅ Connexion réussie pour: " + client.getEmail());

                        // Stocker les informations de session
                        SessionManager.setCurrentClient(client);

                        showMessage("Connexion réussie ! Redirection...", "success");

                        // Redirection vers le dashboard client après un court délai
                        Platform.runLater(() -> {
                            try {
                                Thread.sleep(1000); // Délai pour afficher le message
                                ouvrirDashboardClient(event != null ? event : new ActionEvent(btnLogin, null));
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
                    System.err.println("❌ Identifiants incorrects ou compte suspendu");

                    String errorMessage = e.getMessage();
                    if (errorMessage.contains("suspend")) {
                        showMessage("Votre compte est suspendu. Contactez l'administration.", "error");
                    } else if (errorMessage.contains("serveur")) {
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
    private void handleQuitter(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/UI_Main.fxml", event);
    }

    @FXML
    private void handleInscription(ActionEvent event) {
        System.out.println("📝 Redirection vers inscription...");
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Register.fxml", event);
    }

    private void ouvrirDashboardClient(ActionEvent event) {
        try {
            System.out.println("🚀 Navigation vers le dashboard client...");
            navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Dashboard.fxml", event);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Erreur lors de l'ouverture du dashboard", "error");
            resetLoginButton();
        }
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

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }
}