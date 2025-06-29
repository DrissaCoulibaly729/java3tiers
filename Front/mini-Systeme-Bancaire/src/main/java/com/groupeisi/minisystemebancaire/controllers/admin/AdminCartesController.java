package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.CarteBancaireDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.services.CarteBancaireService;
import com.groupeisi.minisystemebancaire.services.ClientService;
import com.groupeisi.minisystemebancaire.services.CompteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class AdminCartesController {

    // ✅ DÉCLARATION DES COMPOSANTS FXML
    @FXML private TableView<CarteBancaireDTO> tableCartes;
    @FXML private TableColumn<CarteBancaireDTO, String> colNumero, colStatut, colClient;
    @FXML private TableColumn<CarteBancaireDTO, LocalDate> colExpiration;
    @FXML private TableColumn<CarteBancaireDTO, Double> colSolde;

    @FXML private ComboBox<CompteDTO> cmbCompte;
    @FXML private ComboBox<String> cmbTypeCarte;
    @FXML private TextField txtCodePin;
    @FXML private DatePicker dateExpiration;

    @FXML private Button btnCreerCarte, btnBloquerCarte, btnDebloquerCarte, btnSupprimerCarte;
    @FXML private Button btnDashboard, btnClients, btnComptes, btnTransactions, btnCredits, btnSupport, btnDeconnexion;
    @FXML private Label lblMessage;

    // SERVICES
    private final CarteBancaireService carteService = new CarteBancaireService();
    private final CompteService compteService = new CompteService();
    private final ClientService clientService = new ClientService();
    private CarteBancaireDTO selectedCarte;

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation AdminCartesController");
        setupTableColumns();
        setupComboBoxes();
        loadData();
        setupTableSelection();
    }

    private void setupTableColumns() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
    }

    private void setupComboBoxes() {
        cmbTypeCarte.setItems(FXCollections.observableArrayList("Débit", "Crédit"));
        cmbTypeCarte.setValue("Débit");
    }

    private void setupTableSelection() {
        tableCartes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedCarte = newSelection;
            updateButtonStates();
        });
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedCarte != null;
        btnBloquerCarte.setDisable(!hasSelection);
        btnDebloquerCarte.setDisable(!hasSelection);
        btnSupprimerCarte.setDisable(!hasSelection);
    }

    private void loadData() {
        try {
            // Charger les comptes pour le ComboBox
            List<CompteDTO> comptes = compteService.getAllComptes();
            cmbCompte.setItems(FXCollections.observableArrayList(comptes));

            // Charger les cartes
            loadCartes();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données");
        }
    }

    private void loadCartes() {
        try {
            List<CarteBancaireDTO> cartes = carteService.getAllCartes();
            tableCartes.setItems(FXCollections.observableArrayList(cartes));
            System.out.println("✅ " + cartes.size() + " cartes chargées");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement cartes: " + e.getMessage());
            showMessage("Erreur lors du chargement des cartes: " + e.getMessage(), "error");
        }
    }

    // ✅ MÉTHODES PRINCIPALES

    @FXML
    private void handleCreerCarte() {
        System.out.println("💳 Création d'une nouvelle carte");
        if (!validateForm()) return;

        try {
            CarteBancaireDTO carte = new CarteBancaireDTO();
            carte.setCompteId(cmbCompte.getValue().getId());
            carte.setCodePin(txtCodePin.getText().trim());
            carte.setDateExpiration(dateExpiration.getValue());
            carte.setStatut("Active");
            carte.setSolde(0.0);

            carteService.createCarte(carte);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Carte créée avec succès !");
            clearForm();
            loadCartes();
        } catch (Exception e) {
            System.err.println("❌ Erreur création carte: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création : " + e.getMessage());
        }
    }

    @FXML
    private void handleBloquerCarte() {
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une carte à bloquer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Bloquer la carte");
        confirmation.setContentText("Êtes-vous sûr de vouloir bloquer cette carte ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                carteService.bloquerCarte(selectedCarte.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Carte bloquée avec succès");
                loadCartes();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du blocage : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDebloquerCarte() {
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une carte à débloquer");
            return;
        }

        try {
            carteService.debloquerCarte(selectedCarte.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Carte débloquée avec succès");
            loadCartes();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du déblocage : " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimerCarte() {
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner une carte à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer la carte");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette carte ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                carteService.deleteCarte(selectedCarte.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Carte supprimée avec succès");
                loadCartes();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    // ✅ MÉTHODES DE NAVIGATION

    @FXML
    private void handleDashboard() {
        navigateToPage("UI_Dashboard");
    }

    @FXML
    private void handleGestionClients() {
        navigateToPage("UI_Gestion_Clients");
    }

    @FXML
    private void handleGestionComptes() {
        navigateToPage("UI_Gestion_Comptes");
    }

    @FXML
    private void handleGestionTransactions() {
        navigateToPage("UI_Gestion_Transactions");
    }

    @FXML
    private void handleGestionCredits() {
        navigateToPage("UI_Gestion_Credits");
    }

    @FXML
    private void handleGestionSupport() {
        navigateToPage("UI_Service_Client_Rapports");
    }

    @FXML
    private void handleDeconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter");
        }
    }

    // ✅ MÉTHODES UTILITAIRES

    private boolean validateForm() {
        if (cmbCompte.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un compte");
            return false;
        }

        if (txtCodePin.getText().trim().length() != 4) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le code PIN doit contenir 4 chiffres");
            return false;
        }

        if (dateExpiration.getValue() == null || dateExpiration.getValue().isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner une date d'expiration valide");
            return false;
        }

        return true;
    }

    private void clearForm() {
        cmbCompte.setValue(null);
        cmbTypeCarte.setValue("Débit");
        txtCodePin.clear();
        dateExpiration.setValue(LocalDate.now().plusYears(3));
    }

    private void navigateToPage(String pageName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/admin/" + pageName + ".fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnCreerCarte.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            System.out.println("🚀 Navigation vers: " + pageName);
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

    private void showMessage(String message, String type) {
        if (lblMessage != null) {
            lblMessage.setText(message);
            String style = switch (type) {
                case "error" -> "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
                case "success" -> "-fx-text-fill: #27ae60; -fx-font-weight: bold;";
                case "warning" -> "-fx-text-fill: #f39c12; -fx-font-weight: bold;";
                default -> "";
            };
            lblMessage.setStyle(style);
        }
    }
}