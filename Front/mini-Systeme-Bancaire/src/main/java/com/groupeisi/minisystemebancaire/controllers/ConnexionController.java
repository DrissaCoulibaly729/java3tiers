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
        String identifier = emailField.getText().trim(); // Peut être email ou username
        String password = passwordField.getText();

        // Validation des champs
        if (!validerChamps(identifier, password)) {
            return;
        }

        // Afficher l'indicateur de chargement
        setLoading(true);
        clearMessage();

        // Déterminer si c'est un admin ou un client
        if (isAdminFormat(identifier)) {
            // Tentative de connexion admin
            connecterAdmin(identifier, password);
        } else {
            // Tentative de connexion client
            connecterClient(identifier, password);
        }
    }

    private void connecterAdmin(String username, String password) {
        System.out.println("Tentative de connexion admin: " + username);

        AuthService.loginAdmin(username, password)
                .thenAccept(response -> {
                    System.out.println("Réponse reçue: " + response);
                    Platform.runLater(() -> {
                        setLoading(false);
                        if (response != null) {
                            try {
                                showSuccess("Connexion réussie en tant qu'administrateur");
                                System.out.println("Tentative d'ouverture du dashboard admin...");
                                redirectToAdminDashboard();
                                System.out.println("Dashboard admin ouvert avec succès !");
                            } catch (Exception e) {
                                System.out.println("ERREUR lors de la redirection: " + e.getMessage());
                                e.printStackTrace();
                                showError("Erreur lors de la redirection: " + e.getMessage());
                            }
                        } else {
                            showError("Identifiants administrateur invalides");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    System.out.println("Erreur complète: " + throwable.getMessage());
                    throwable.printStackTrace();
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Erreur de connexion administrateur: " + throwable.getMessage());
                    });
                    return null;
                });
    }

    private void connecterClient(String email, String password) {
        AuthService.login(email, password)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        if (response != null && response.getToken() != null) {
                            try {
                                showSuccess("Connexion réussie");
                                redirectToClientDashboard();
                            } catch (Exception e) {
                                showError("Erreur lors de la redirection: " + e.getMessage());
                            }
                        } else {
                            showError("Email ou mot de passe incorrect");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Erreur de connexion: " + getErrorMessage(throwable));
                    });
                    return null;
                });
    }

    private boolean validerChamps(String identifier, String password) {
        if (!ValidationUtils.isNotEmpty(identifier)) {
            showError("Veuillez saisir votre email ou nom d'utilisateur");
            emailField.requestFocus();
            return false;
        }

        // Validation spécifique selon le type
        if (!isAdminFormat(identifier)) {
            // Pour les clients, vérifier que c'est un email valide
            if (!ValidationUtils.isValidEmail(identifier)) {
                showError("Format d'email invalide");
                emailField.requestFocus();
                return false;
            }
        }
        // Pour les admins, pas de validation spéciale sur le username

        if (!ValidationUtils.isNotEmpty(password)) {
            showError("Veuillez saisir votre mot de passe");
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isAdminFormat(String identifier) {
        // Détermine si l'identifiant ressemble à un username admin
        // Logique: si ça ne contient pas @ et fait moins de 50 caractères, c'est probablement un username admin
        return !identifier.contains("@") && identifier.length() < 50;
    }

    private void redirectToClientDashboard() throws Exception {
        WindowManager.closeWindow();
        WindowManager.openWindow("/com/groupeisi/minisystemebancaire/client/dashboard-client.fxml", "Espace Client - Tableau de bord");
    }

    private void redirectToAdminDashboard() throws Exception {
        // Debug : voir ce qui est disponible
        System.out.println("Ressource trouvée : " + WindowManager.class.getResource("/com/groupeisi/minisystemebancaire/admin/"));

        WindowManager.closeWindow();
        WindowManager.openWindow("/com/groupeisi/minisystemebancaire/admin/test-admin.fxml", "Administration - Tableau de bord");
    }

    private String getErrorMessage(Throwable throwable) {
        String errorMessage = "Erreur de connexion";
        if (throwable.getCause() != null) {
            String causeMessage = throwable.getCause().getMessage();
            if (causeMessage.contains("401")) {
                errorMessage = "Identifiants invalides";
            } else if (causeMessage.contains("500")) {
                errorMessage = "Erreur serveur. Veuillez réessayer plus tard";
            } else {
                errorMessage = "Impossible de se connecter au serveur";
            }
        }
        return errorMessage;
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