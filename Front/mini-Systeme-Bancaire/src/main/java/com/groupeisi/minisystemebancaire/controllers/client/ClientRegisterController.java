package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.ClientService;
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

public class ClientRegisterController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextArea txtAdresse;  // ✅ Changé en TextArea
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnInscrire;
    @FXML private Button btnRetour;
    @FXML private Label lblMessage;

    private final ClientService clientService = new ClientService();

    @FXML
    public void initialize() {
        lblMessage.setText("");
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        btnInscrire.setOnAction(this::handleInscription);
        btnRetour.setOnAction(this::handleRetour);
    }

    @FXML
    private void handleInscription(ActionEvent event) {
        // Récupération des données du formulaire
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String adresse = txtAdresse.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        // Validation des champs
        if (!validateForm(nom, prenom, email, telephone, adresse, password, confirmPassword)) {
            return;
        }

        // Désactiver le bouton pendant l'inscription
        btnInscrire.setDisable(true);
        btnInscrire.setText("Inscription en cours...");

        // Exécuter l'inscription dans un thread séparé
        Thread registerThread = new Thread(() -> {
            try {
                System.out.println("📝 Tentative d'inscription client...");

                ClientDTO client = new ClientDTO(nom, prenom, email, telephone, adresse, password);
                ClientDTO clientCree = clientService.registerClient(client);

                Platform.runLater(() -> {
                    if (clientCree != null) {
                        System.out.println("✅ Inscription réussie pour: " + clientCree.getEmail());

                        showMessage("Inscription réussie ! Vous pouvez maintenant vous connecter.", "success");

                        // Redirection vers la page de connexion après inscription
                        Platform.runLater(() -> {
                            try {
                                Thread.sleep(2000); // Délai pour lire le message
                                navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Login.fxml", event);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        });
                    } else {
                        showMessage("Erreur lors de l'inscription", "error");
                        resetRegisterButton();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("❌ Erreur lors de l'inscription: " + e.getMessage());

                    String errorMessage = e.getMessage();
                    if (errorMessage.contains("email") && errorMessage.contains("unique")) {
                        showMessage("Cette adresse email est déjà utilisée", "error");
                    } else if (errorMessage.contains("serveur")) {
                        showMessage("Erreur de connexion au serveur", "error");
                    } else {
                        showMessage("Erreur lors de l'inscription: " + errorMessage, "error");
                    }

                    resetRegisterButton();
                });
            }
        });

        registerThread.setDaemon(true);
        registerThread.start();
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Login.fxml", event);
    }

    private boolean validateForm(String nom, String prenom, String email, String telephone,
                                 String adresse, String password, String confirmPassword) {

        // Vérifier que tous les champs sont remplis
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() ||
                telephone.isEmpty() || adresse.isEmpty() || password.isEmpty()) {
            showMessage("Tous les champs sont obligatoires", "error");
            return false;
        }

        // Validation du nom et prénom
        if (nom.length() < 2 || prenom.length() < 2) {
            showMessage("Le nom et le prénom doivent contenir au moins 2 caractères", "error");
            return false;
        }

        // Validation de l'email
        if (!isValidEmail(email)) {
            showMessage("Format d'email invalide", "error");
            return false;
        }

        // Validation du téléphone
        if (!isValidPhone(telephone)) {
            showMessage("Format de téléphone invalide (ex: +221XXXXXXXXX)", "error");
            return false;
        }

        // Validation de l'adresse
        if (adresse.length() < 5) {
            showMessage("L'adresse doit contenir au moins 5 caractères", "error");
            return false;
        }

        // Validation du mot de passe
        if (password.length() < 6) {
            showMessage("Le mot de passe doit contenir au moins 6 caractères", "error");
            return false;
        }

        // Vérification de la confirmation du mot de passe
        if (!password.equals(confirmPassword)) {
            showMessage("Les mots de passe ne correspondent pas", "error");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        // Accepte les formats : +221XXXXXXXXX, 221XXXXXXXXX, 0XXXXXXXXX, XXXXXXXXX
        String phoneRegex = "^(\\+221|221|0)?[0-9]{9}$";
        return Pattern.compile(phoneRegex).matcher(phone.replaceAll("\\s", "")).matches();
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
            showMessage("Erreur lors de la navigation", "error");
        }
    }

    private void showMessage(String message, String type) {
        lblMessage.setText(message);
        lblMessage.setStyle(type.equals("error") ?
                "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" :
                "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void resetRegisterButton() {
        btnInscrire.setDisable(false);
        btnInscrire.setText("S'inscrire");
    }
}