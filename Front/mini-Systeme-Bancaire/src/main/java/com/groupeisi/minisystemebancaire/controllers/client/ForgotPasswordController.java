package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.services.AuthService;
import com.groupeisi.minisystemebancaire.services.HttpService;
import com.groupeisi.minisystemebancaire.utils.ValidationUtils;
import com.groupeisi.minisystemebancaire.utils.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ForgotPasswordController implements Initializable {

    // Étape 1 : Demande de réinitialisation
    @FXML private TextField emailField;
    @FXML private Button envoyerLienButton;
    @FXML private Label messageLabel;

    // Étape 2 : Réinitialisation avec token
    @FXML private TextField tokenField;
    @FXML private PasswordField nouveauPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetPasswordButton;

    // Navigation
    @FXML private Button retourConnexionButton;
    @FXML private Hyperlink retourConnexionLink;

    // Conteneurs pour les étapes
    @FXML private VBox etapeDemandeContainer;
    @FXML private VBox etapeResetContainer;

    // Indicateur de chargement
    @FXML private ProgressIndicator loadingIndicator;

    private String emailSaisi;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerInterface();
        configurerBoutons();
        afficherEtapedemande();
    }

    private void configurerInterface() {
        // Masquer l'indicateur de chargement
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        // Configuration des champs
        if (emailField != null) {
            emailField.setPromptText("Votre adresse email");
        }
        if (tokenField != null) {
            tokenField.setPromptText("Code de réinitialisation reçu par email");
        }
        if (nouveauPasswordField != null) {
            nouveauPasswordField.setPromptText("Nouveau mot de passe");
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText("Confirmer le nouveau mot de passe");
        }
    }

    private void configurerBoutons() {
        if (envoyerLienButton != null) {
            envoyerLienButton.setOnAction(e -> envoyerLienReinitialisation());
        }
        if (resetPasswordButton != null) {
            resetPasswordButton.setOnAction(e -> reinitialiserMotDePasse());
        }
        if (retourConnexionButton != null) {
            retourConnexionButton.setOnAction(e -> retournerAConnexion());
        }
        if (retourConnexionLink != null) {
            retourConnexionLink.setOnAction(e -> retournerAConnexion());
        }

        // Permettre l'envoi avec Entrée
        if (emailField != null) {
            emailField.setOnAction(e -> envoyerLienReinitialisation());
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setOnAction(e -> reinitialiserMotDePasse());
        }
    }

    private void afficherEtapedemande() {
        if (etapeDemandeContainer != null) {
            etapeDemandeContainer.setVisible(true);
            etapeDemandeContainer.setManaged(true);
        }
        if (etapeResetContainer != null) {
            etapeResetContainer.setVisible(false);
            etapeResetContainer.setManaged(false);
        }
        clearMessage();
    }

    private void afficherEtapeReset() {
        if (etapeDemandeContainer != null) {
            etapeDemandeContainer.setVisible(false);
            etapeDemandeContainer.setManaged(false);
        }
        if (etapeResetContainer != null) {
            etapeResetContainer.setVisible(true);
            etapeResetContainer.setManaged(true);
        }
        clearMessage();
    }

    @FXML
    private void envoyerLienReinitialisation() {
        String email = emailField.getText().trim();

        // Validation
        if (!validerEmail(email)) {
            return;
        }

        // Sauvegarder l'email pour l'étape suivante
        emailSaisi = email;

        // Afficher l'indicateur de chargement
        setLoading(true);
        clearMessage();

        // Envoyer la demande
        AuthService.forgotPassword(email)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        setLoading(false);

                        if (success) {
                            showSuccess("Email envoyé ! Vérifiez votre boîte de réception.");

                            // Passer à l'étape de réinitialisation
                            Platform.runLater(() -> {
                                try {
                                    Thread.sleep(2000); // Attendre 2 secondes pour que l'utilisateur lise le message
                                    afficherEtapeReset();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            });
                        } else {
                            showError("Erreur lors de l'envoi de l'email. Vérifiez votre adresse.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setLoading(false);

                        // Gérer les différents types d'erreurs
                        String errorMessage = "Erreur lors de l'envoi";

                        if (throwable.getCause() != null) {
                            String causeMessage = throwable.getCause().getMessage();
                            if (causeMessage.contains("404")) {
                                errorMessage = "Cette adresse email n'est pas enregistrée";
                            } else if (causeMessage.contains("429")) {
                                errorMessage = "Trop de tentatives. Veuillez attendre avant de réessayer";
                            } else if (causeMessage.contains("500")) {
                                errorMessage = "Erreur serveur. Veuillez réessayer plus tard";
                            } else {
                                errorMessage = "Impossible de contacter le serveur";
                            }
                        }

                        showError(errorMessage);
                    });
                    return null;
                });
    }

    @FXML
    private void reinitialiserMotDePasse() {
        String token = tokenField.getText().trim();
        String nouveauPassword = nouveauPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (!validerResetForm(token, nouveauPassword, confirmPassword)) {
            return;
        }

        // Afficher l'indicateur de chargement
        setLoading(true);
        clearMessage();

        // Envoyer la demande de réinitialisation
        AuthService.resetPassword(token, emailSaisi, nouveauPassword, confirmPassword)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        setLoading(false);

                        if (success) {
                            WindowManager.showSuccess("Succès",
                                    "Mot de passe réinitialisé",
                                    "Votre mot de passe a été réinitialisé avec succès. " +
                                            "Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.");

                            // Retourner à la page de connexion
                            retournerAConnexion();
                        } else {
                            showError("Erreur lors de la réinitialisation. Vérifiez le code saisi.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        setLoading(false);

                        // Gérer les erreurs
                        String errorMessage = "Erreur lors de la réinitialisation";

                        if (throwable.getCause() != null) {
                            String causeMessage = throwable.getCause().getMessage();
                            if (causeMessage.contains("400")) {
                                errorMessage = "Code de réinitialisation invalide ou expiré";
                            } else if (causeMessage.contains("422")) {
                                errorMessage = "Le mot de passe ne respecte pas les critères de sécurité";
                            } else {
                                errorMessage = "Erreur serveur. Veuillez réessayer";
                            }
                        }

                        showError(errorMessage);
                    });
                    return null;
                });
    }

    @FXML
    private void retourEtapePrecedente() {
        afficherEtapedemande();

        // Vider les champs de l'étape reset
        if (tokenField != null) tokenField.clear();
        if (nouveauPasswordField != null) nouveauPasswordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
    }

    @FXML
    private void renvoyerEmail() {
        if (emailSaisi != null && !emailSaisi.isEmpty()) {
            emailField.setText(emailSaisi);
            afficherEtapedemande();
            envoyerLienReinitialisation();
        } else {
            afficherEtapedemande();
        }
    }

    private boolean validerEmail(String email) {
        if (!ValidationUtils.isNotEmpty(email)) {
            showError("Veuillez saisir votre adresse email");
            emailField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Format d'email invalide");
            emailField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validerResetForm(String token, String nouveauPassword, String confirmPassword) {
        if (!ValidationUtils.isNotEmpty(token)) {
            showError("Veuillez saisir le code de réinitialisation");
            tokenField.requestFocus();
            return false;
        }

        if (token.length() < 6) {
            showError("Le code de réinitialisation doit contenir au moins 6 caractères");
            tokenField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(nouveauPassword)) {
            showError("Veuillez saisir un nouveau mot de passe");
            nouveauPasswordField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidSimplePassword(nouveauPassword)) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            nouveauPasswordField.requestFocus();
            return false;
        }

        if (!nouveauPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            confirmPasswordField.requestFocus();
            return false;
        }

        return true;
    }

    private void setLoading(boolean loading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(loading);
        }

        // Désactiver les boutons pendant le chargement
        if (envoyerLienButton != null) {
            envoyerLienButton.setDisable(loading);
            envoyerLienButton.setText(loading ? "Envoi en cours..." : "Envoyer le lien");
        }
        if (resetPasswordButton != null) {
            resetPasswordButton.setDisable(loading);
            resetPasswordButton.setText(loading ? "Réinitialisation..." : "Réinitialiser");
        }

        // Désactiver les champs
        if (emailField != null) emailField.setDisable(loading);
        if (tokenField != null) tokenField.setDisable(loading);
        if (nouveauPasswordField != null) nouveauPasswordField.setDisable(loading);
        if (confirmPasswordField != null) confirmPasswordField.setDisable(loading);
    }

    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-text-fill: red;");
        } else {
            WindowManager.showError("Erreur", "Erreur", message);
        }
    }

    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            WindowManager.showSuccess("Succès", "Succès", message);
        }
    }

    private void clearMessage() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    private void retournerAConnexion() {
        try {
            WindowManager.closeWindow();
            WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}