package com.groupeisi.minisystemebancaire.controllers;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.services.AuthService;
import com.groupeisi.minisystemebancaire.utils.ValidationUtils;
import com.groupeisi.minisystemebancaire.utils.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ConnexionController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button connexionButton;
    @FXML private Button inscriptionButton;
    @FXML private Hyperlink motDePasseOublieLink;
    @FXML private Label messageLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Masquer l'indicateur de chargement au démarrage
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        // Configurer les actions
        connexionButton.setOnAction(e -> seConnecter());

        if (inscriptionButton != null) {
            inscriptionButton.setOnAction(e -> ouvrirInscription());
        }

        if (motDePasseOublieLink != null) {
            motDePasseOublieLink.setOnAction(e -> ouvrirMotDePasseOublie());
        }

        // Permettre la connexion avec Entrée
        passwordField.setOnAction(e -> seConnecter());
    }

    @FXML
    private void seConnecter() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation des champs
        if (!validerChamps(email, password)) {
            return;
        }

        // Afficher l'indicateur de chargement
        setLoading(true);
        clearMessage();

        // Effectuer la connexion
        AuthService.login(email, password)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        setLoading(false);

                        if (response != null && response.getToken() != null) {
                            // Connexion réussie
                            try {
                                // Rediriger vers le tableau de bord approprié
                                redirectToDashboard(response.getUser().getEmail());
                            } catch (Exception e) {
                                showError("Erreur lors de l'ouverture du tableau de bord: " + e.getMessage());
                            }
                        } else {
                            showError("Identifiants invalides");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setLoading(false);

                        // Gérer les différents types d'erreurs
                        String errorMessage = "Erreur de connexion";

                        if (throwable.getCause() != null) {
                            String causeMessage = throwable.getCause().getMessage();
                            if (causeMessage.contains("401")) {
                                errorMessage = "Email ou mot de passe incorrect";
                            } else if (causeMessage.contains("403")) {
                                errorMessage = "Compte désactivé";
                            } else if (causeMessage.contains("500")) {
                                errorMessage = "Erreur serveur, veuillez réessayer plus tard";
                            } else {
                                errorMessage = "Impossible de se connecter au serveur";
                            }
                        }

                        showError(errorMessage);
                    });
                    return null;
                });
    }

    private boolean validerChamps(String email, String password) {
        if (!ValidationUtils.isNotEmpty(email)) {
            showError("Veuillez saisir votre email");
            emailField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Format d'email invalide");
            emailField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(password)) {
            showError("Veuillez saisir votre mot de passe");
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    private void redirectToDashboard(String userEmail) throws Exception {
        // Vérifier si c'est un admin ou un client
        if (isAdmin(userEmail)) {
            WindowManager.closeWindow();
            WindowManager.openWindow("/fxml/admin/dashboard-admin.fxml", "Administration - Tableau de bord");
        } else {
            WindowManager.closeWindow();
            WindowManager.openWindow("/fxml/client/dashboard-client.fxml", "Espace Client - Tableau de bord");
        }
    }

    private boolean isAdmin(String email) {
        // Logique pour déterminer si l'utilisateur est admin
        // Cela pourrait être basé sur l'email ou sur un rôle retourné par l'API
        return email.contains("admin") || email.endsWith("@admin.bank");
    }

    @FXML
    private void ouvrirInscription() {
        try {
            WindowManager.openWindow("/fxml/inscription.fxml", "Inscription");
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la fenêtre d'inscription: " + e.getMessage());
        }
    }

    @FXML
    private void ouvrirMotDePasseOublie() {
        try {
            WindowManager.openWindow("/fxml/forgot-password.fxml", "Mot de passe oublié");
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la fenêtre: " + e.getMessage());
        }
    }

    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }
        connexionButton.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
    }

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-text-fill: red;");
        } else {
            WindowManager.showError("Erreur", "Erreur de connexion", message);
        }
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-text-fill: green;");
        }
    }

    private void clearMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }
}