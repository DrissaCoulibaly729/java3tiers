package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.CompteService;
import com.groupeisi.minisystemebancaire.services.ClientService;
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
import java.util.List;

public class AdminComptesController {

    @FXML private TextField txtSoldeInitial, txtMontantFrais, txtRechercheCompte;
    @FXML private ChoiceBox<String> choiceTypeCompte, choiceTypeFrais;
    @FXML private ChoiceBox<ClientDTO> choiceClient;
    @FXML private ChoiceBox<CompteDTO> choiceCompteFrais;
    @FXML private TableView<CompteDTO> tableComptes;
    @FXML private TableColumn<CompteDTO, String> colNumeroCompte, colType, colStatut, colClient;
    @FXML private TableColumn<CompteDTO, Double> colSolde;
    @FXML private Button btnOuvrirCompte, btnAppliquerFrais, btnModifierCompte, btnFermerCompte, btnDeconnexion;

    private final CompteService compteService = new CompteService();
    private final ClientService clientService = new ClientService();
    private CompteDTO selectedCompte;

    @FXML
    private void initialize() {
        setupComponents();
        setupTableColumns();
        loadData();
        setupTableSelection();
    }

    private void setupComponents() {
        choiceTypeCompte.setItems(FXCollections.observableArrayList("Courant", "Épargne"));
        choiceTypeFrais.setItems(FXCollections.observableArrayList("Mensuel", "Transaction", "Maintenance"));
    }

    private void setupTableColumns() {
        colNumeroCompte.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colClient.setCellValueFactory(cellData -> {
            CompteDTO compte = cellData.getValue();
            if (compte.getClient() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        compte.getClient().getNom() + " " + compte.getClient().getPrenom()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
    }

    private void setupTableSelection() {
        tableComptes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedCompte = newSelection;
        });
    }

    @FXML
    private void handleOuvrirCompte() {
        if (!validateCompteForm()) return;

        try {
            // ✅ CORRECTION : Utiliser setters au lieu de constructeur problématique
            CompteDTO compte = new CompteDTO();
            compte.setType(choiceTypeCompte.getValue());
            compte.setSolde(Double.parseDouble(txtSoldeInitial.getText()));
            compte.setClientId(choiceClient.getValue().getId());
            compte.setStatut("Actif");

            compteService.createCompte(compte);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte ouvert avec succès !");
            clearForm();
            loadComptes();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ouverture : " + e.getMessage());
        }
    }

    @FXML
    private void handleAppliquerFrais() {
        if (choiceCompteFrais.getValue() == null || choiceTypeFrais.getValue() == null ||
                txtMontantFrais.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs pour les frais");
            return;
        }

        try {
            double montant = Double.parseDouble(txtMontantFrais.getText());

            // ✅ CORRECTION : Utiliser la signature correcte (Long, String, Double)
            compteService.appliquerFrais(
                    choiceCompteFrais.getValue().getId(),
                    choiceTypeFrais.getValue(),
                    montant
            );

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Frais appliqués avec succès !");
            clearForm();
            loadComptes();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être un nombre valide");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'application des frais : " + e.getMessage());
        }
    }


    @FXML
    private void handleModifierCompte() {
        if (selectedCompte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un compte");
            return;
        }

        try {
            // ✅ CORRECTION : Utiliser updateCompte avec un seul paramètre
            selectedCompte.setType(choiceTypeCompte.getValue());
            compteService.updateCompte(selectedCompte);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte modifié avec succès !");
            loadComptes();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void handleFermerCompte() {
        if (selectedCompte == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un compte");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setContentText("Êtes-vous sûr de vouloir fermer ce compte ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                compteService.fermerCompte(selectedCompte.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte fermé avec succès");
                loadComptes();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }

    private boolean validateCompteForm() {
        if (choiceTypeCompte.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un type de compte");
            return false;
        }

        if (choiceClient.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un client");
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

        return true;
    }

    private void clearForm() {
        choiceTypeCompte.getSelectionModel().clearSelection();
        choiceClient.getSelectionModel().clearSelection();
        choiceCompteFrais.getSelectionModel().clearSelection();
        choiceTypeFrais.getSelectionModel().clearSelection();
        txtSoldeInitial.clear();
        txtMontantFrais.clear();
        selectedCompte = null;
        tableComptes.getSelectionModel().clearSelection();
    }

    private void loadData() {
        try {
            // Charger les clients
            List<ClientDTO> clients = clientService.getAllClients();
            choiceClient.setItems(FXCollections.observableArrayList(clients));

            loadComptes();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données");
        }
    }

    private void loadComptes() {
        try {
            List<CompteDTO> comptes = compteService.getAllComptes();
            ObservableList<CompteDTO> comptesData = FXCollections.observableArrayList(comptes);
            tableComptes.setItems(comptesData);
            choiceCompteFrais.setItems(comptesData);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les comptes");
        }
    }

    // Navigation methods
    @FXML private void handleDashboard() { navigateToPage("UI_Dashboard"); }
    @FXML private void handleGestionClients() { navigateToPage("UI_Gestion_Clients"); }
    @FXML private void handleGestionTransactions() { navigateToPage("UI_Gestion_Transactions"); }
    @FXML private void handleGestionCredits() { navigateToPage("UI_Gestion_Credits"); }
    @FXML private void handleGestionCartes() { navigateToPage("UI_Gestion_Cartes_Bancaires"); }

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
            Stage stage = (Stage) btnOuvrirCompte.getScene().getWindow();
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