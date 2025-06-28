package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.CarteBancaireDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.services.CarteBancaireService;
import com.groupeisi.minisystemebancaire.services.CompteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminCartesController {

    @FXML private ChoiceBox<CompteDTO> choiceCompte;
    @FXML private TextField txtSoldeInitial;
    @FXML private TextField txtNumero;
    @FXML private TextField txtCVV;
    @FXML private DatePicker txtDateExpiration;
    @FXML private TextField txtCodePin;
    @FXML private TextField txtRechercheCartes;

    @FXML private TableView<CarteBancaireDTO> tableCartes;
    @FXML private TableColumn<CarteBancaireDTO, String> colNumero;
    @FXML private TableColumn<CarteBancaireDTO, String> colCVV;
    @FXML private TableColumn<CarteBancaireDTO, LocalDate> colDateExpiration;
    @FXML private TableColumn<CarteBancaireDTO, Double> colSolde;
    @FXML private TableColumn<CarteBancaireDTO, String> colStatut;
    @FXML private TableColumn<CarteBancaireDTO, String> colCompte;

    @FXML private Button btnCreerCarte;
    @FXML private Button btnBloquerCarte;
    @FXML private Button btnDebloquerCarte;
    @FXML private Button btnSupprimerCarte;
    @FXML private Button btnDeconnexion;

    private final CarteBancaireService carteBancaireService = new CarteBancaireService();
    private final CompteService compteService = new CompteService();

    @FXML
    private void initialize() {
        setupTableColumns();
        loadData();
    }

    private void setupTableColumns() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colCVV.setCellValueFactory(new PropertyValueFactory<>("cvv"));
        colDateExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Affichage du compte associé
        colCompte.setCellValueFactory(cellData -> {
            CarteBancaireDTO carte = cellData.getValue();
            if (carte.getCompte() != null) {
                return new javafx.beans.property.SimpleStringProperty(carte.getCompte().getNumero());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
    }

    private void loadData() {
        try {
            // Charger les comptes pour la ChoiceBox
            List<CompteDTO> comptes = compteService.getAllComptes();
            choiceCompte.setItems(FXCollections.observableArrayList(comptes));

            // Charger toutes les cartes
            refreshCartes();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données");
        }
    }

    @FXML
    private void handleCreerCarte() {
        if (!validateCarteForm()) {
            return;
        }

        try {
            // ✅ CORRECTION : Utiliser le constructeur approprié ou setters
            CarteBancaireDTO carte = new CarteBancaireDTO();

            // Option 1 : Utiliser les setters (RECOMMANDÉ)
            carte.setNumero(txtNumero.getText().trim());
            carte.setCvv(txtCVV.getText().trim());
            carte.setDateExpiration(txtDateExpiration.getValue());
            carte.setSolde(Double.parseDouble(txtSoldeInitial.getText()));
            carte.setStatut("Active");
            carte.setCompteId(choiceCompte.getValue().getId());
            carte.setCodePin(txtCodePin.getText().trim());

            // Option 2 : Utiliser le constructeur complet (alternative)
            /*
            CarteBancaireDTO carte = new CarteBancaireDTO(
                txtNumero.getText().trim(),
                txtCVV.getText().trim(),
                txtDateExpiration.getValue(),
                Double.parseDouble(txtSoldeInitial.getText()),
                "Active",
                choiceCompte.getValue().getId(),
                txtCodePin.getText().trim()
            );
            */

            carteBancaireService.createCarte(carte);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Carte bancaire créée avec succès !");
            clearForm();
            refreshCartes();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création : " + e.getMessage());
        }
    }

    private boolean validateCarteForm() {
        if (choiceCompte.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un compte");
            return false;
        }

        if (txtSoldeInitial.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir le solde initial");
            return false;
        }

        try {
            double solde = Double.parseDouble(txtSoldeInitial.getText());
            if (solde < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le solde doit être positif");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le solde doit être un nombre valide");
            return false;
        }

        // Validation du numéro de carte (16 chiffres)
        if (txtNumero.getText().trim().length() != 16 || !txtNumero.getText().matches("\\d{16}")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le numéro de carte doit contenir 16 chiffres");
            return false;
        }

        // Validation du CVV (3 chiffres)
        if (txtCVV.getText().trim().length() != 3 || !txtCVV.getText().matches("\\d{3}")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le CVV doit contenir 3 chiffres");
            return false;
        }

        // Validation de la date d'expiration
        if (txtDateExpiration.getValue() == null || txtDateExpiration.getValue().isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La date d'expiration doit être dans le futur");
            return false;
        }

        // Validation du code PIN (4 chiffres)
        if (txtCodePin.getText().trim().length() != 4 || !txtCodePin.getText().matches("\\d{4}")) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le code PIN doit contenir 4 chiffres");
            return false;
        }

        return true;
    }

    @FXML
    private void handleBloquerCarte() {
        CarteBancaireDTO selectedCarte = tableCartes.getSelectionModel().getSelectedItem();
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une carte");
            return;
        }

        try {
            carteBancaireService.bloquerCarte(selectedCarte.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Carte bloquée avec succès");
            refreshCartes();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du blocage : " + e.getMessage());
        }
    }

    @FXML
    private void handleDebloquerCarte() {
        CarteBancaireDTO selectedCarte = tableCartes.getSelectionModel().getSelectedItem();
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une carte");
            return;
        }

        try {
            carteBancaireService.debloquerCarte(selectedCarte.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Carte débloquée avec succès");
            refreshCartes();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du déblocage : " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimerCarte() {
        CarteBancaireDTO selectedCarte = tableCartes.getSelectionModel().getSelectedItem();
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une carte");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette carte ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                carteBancaireService.deleteCarte(selectedCarte.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Carte supprimée avec succès");
                refreshCartes();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        choiceCompte.getSelectionModel().clearSelection();
        txtSoldeInitial.clear();
        txtNumero.clear();
        txtCVV.clear();
        txtDateExpiration.setValue(null);
        txtCodePin.clear();
    }

    private void refreshCartes() {
        try {
            List<CarteBancaireDTO> cartes = carteBancaireService.getAllCartes();
            ObservableList<CarteBancaireDTO> cartesData = FXCollections.observableArrayList(cartes);
            tableCartes.setItems(cartesData);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de rafraîchir les cartes");
        }
    }

    // Navigation methods
    @FXML private void handleDashboard() { navigateToPage("UI_Dashboard"); }
    @FXML private void handleGestionClients() { navigateToPage("UI_Gestion_Clients"); }
    @FXML private void handleGestionComptes() { navigateToPage("UI_Gestion_Comptes"); }
    @FXML private void handleGestionTransactions() { navigateToPage("UI_Gestion_Transactions"); }
    @FXML private void handleGestionCredits() { navigateToPage("UI_Gestion_Credits"); }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToPage(String pageName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/admin/" + pageName + ".fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnCreerCarte.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page " + pageName);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}