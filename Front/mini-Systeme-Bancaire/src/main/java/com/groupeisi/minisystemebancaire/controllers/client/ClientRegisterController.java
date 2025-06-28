package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.ClientService;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.regex.Pattern;

public class ClientRegisterController {

    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextArea txtAdresse;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnRegister;
    @FXML private Button btnRetour;
    @FXML private Label lblMessage;

    private final ClientService clientService = new ClientService();

    @FXML
    private void initialize() {
        lblMessage.setText("");
        setupFieldValidation();
    }

    private void setupFieldValidation() {
        // Validation en temps réel
        txtEmail.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty() && !isValidEmail(newText)) {
                txtEmail.setStyle("-fx-border-color: red;");
            } else {
                txtEmail.setStyle("");
            }
        });

        txtTelephone.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty() && !isValidPhone(newText)) {
                txtTelephone.setStyle("-fx-border-color: red;");
            } else {
                txtTelephone.setStyle("");
            }
        });

        txtConfirmPassword.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty() && !newText.equals(txtPassword.getText())) {
                txtConfirmPassword.setStyle("-fx-border-color: red;");
            } else {
                txtConfirmPassword.setStyle("");
            }
        });
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            ClientDTO client = new ClientDTO(
                    txtNom.getText().trim(),
                    txtPrenom.getText().trim(),
                    txtEmail.getText().trim(),
                    txtTelephone.getText().trim(),
                    txtAdresse.getText().trim(),
                    txtPassword.getText()
            );

            clientService.createClient(client);

            showMessage("Inscription réussie ! Vous pouvez maintenant vous connecter.", "success");

            // CORRECTION : Utilisation correcte de PauseTransition
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> handleRetour(event));
            delay.play();

        } catch (Exception e) {
            if (e.getMessage().contains("email")) {
                showMessage("Cette adresse email est déjà utilisée", "error");
            } else {
                showMessage("Erreur lors de l'inscription : " + e.getMessage(), "error");
            }
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        navigateTo("/com/groupeisi/minisystemebancaire/client/UI_Login.fxml", event);
    }

    private boolean validateForm() {
        // Validation des champs obligatoires
        if (txtNom.getText().trim().isEmpty()) {
            showMessage("Le nom est obligatoire", "error");
            txtNom.requestFocus();
            return false;
        }

        if (txtPrenom.getText().trim().isEmpty()) {
            showMessage("Le prénom est obligatoire", "error");
            txtPrenom.requestFocus();
            return false;
        }

        if (txtEmail.getText().trim().isEmpty()) {
            showMessage("L'email est obligatoire", "error");
            txtEmail.requestFocus();
            return false;
        }

        if (!isValidEmail(txtEmail.getText().trim())) {
            showMessage("L'adresse email n'est pas valide", "error");
            txtEmail.requestFocus();
            return false;
        }

        if (txtTelephone.getText().trim().isEmpty()) {
            showMessage("Le téléphone est obligatoire", "error");
            txtTelephone.requestFocus();
            return false;
        }

        if (!isValidPhone(txtTelephone.getText().trim())) {
            showMessage("Le numéro de téléphone n'est pas valide", "error");
            txtTelephone.requestFocus();
            return false;
        }

        if (txtAdresse.getText().trim().isEmpty()) {
            showMessage("L'adresse est obligatoire", "error");
            txtAdresse.requestFocus();
            return false;
        }

        if (txtPassword.getText().length() < 6) {
            showMessage("Le mot de passe doit contenir au moins 6 caractères", "error");
            txtPassword.requestFocus();
            return false;
        }

        if (!txtPassword.getText().equals(txtConfirmPassword.getText())) {
            showMessage("Les mots de passe ne correspondent pas", "error");
            txtConfirmPassword.requestFocus();
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
