package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.CreditDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.CreditService;
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

public class AdminCreditsController {

    @FXML private TextField txtRechercheCredit, txtMontantCredit, txtDureeCredit, txtTauxInteret;
    @FXML private ChoiceBox<ClientDTO> choiceClientCredit;
    @FXML private TableView<CreditDTO> tableCreditsAttente, tableCreditsEnCours;
    @FXML private TableColumn<CreditDTO, Long> colIdCredit, colIdCreditCours;
    @FXML private TableColumn<CreditDTO, String> colClientCredit, colClientCreditCours, colStatutCredit, colStatutCreditCours;
    @FXML private TableColumn<CreditDTO, Double> colMontantCredit, colMontantCreditCours, colMensualiteCreditCours, colTauxInteret;
    @FXML private TableColumn<CreditDTO, Integer> colDureeCredit;
    @FXML private Button btnValiderCredit, btnAnnulerCredit, btnAccepterCredit, btnRefuserCredit, btnVoirDetailsCredit, btnDeconnexion;

    private final CreditService creditService = new CreditService();
    private final ClientService clientService = new ClientService();

    @FXML
    private void initialize() {
        setupTableColumns();
        loadData();
    }

    private void setupTableColumns() {
        // Table crédits en attente - seulement les propriétés qui existent sûrement
        colIdCredit.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMontantCredit.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colTauxInteret.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
        colStatutCredit.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Table crédits en cours
        colIdCreditCours.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMontantCreditCours.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatutCreditCours.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Client avec gestion d'erreur
        colClientCredit.setCellValueFactory(cellData -> {
            try {
                CreditDTO credit = cellData.getValue();
                if (credit.getClient() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            credit.getClient().getNom() + " " + credit.getClient().getPrenom()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("Client ID: " + credit.getClientId());
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        colClientCreditCours.setCellValueFactory(cellData -> {
            try {
                CreditDTO credit = cellData.getValue();
                if (credit.getClient() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            credit.getClient().getNom() + " " + credit.getClient().getPrenom()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("Client ID: " + credit.getClientId());
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
    }

    @FXML
    private void handleValiderCredit() {
        if (!validateCreditForm()) return;

        try {
            // Création avec VOTRE constructeur exact
            Long clientId = choiceClientCredit.getValue().getId();
            Double montant = Double.parseDouble(txtMontantCredit.getText());
            Integer duree = Integer.parseInt(txtDureeCredit.getText());
            Double tauxInteret = Double.parseDouble(txtTauxInteret.getText());

            // VOTRE constructeur : (Long clientId, Double montant, Integer duree, Double tauxInteret)
            CreditDTO credit = new CreditDTO(clientId, montant, duree, tauxInteret);
            creditService.createCredit(credit);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande de crédit soumise avec succès !");
            clearForm();
            loadCredits();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleAccepterCredit() {
        CreditDTO selectedCredit = tableCreditsAttente.getSelectionModel().getSelectedItem();
        if (selectedCredit == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un crédit");
            return;
        }

        try {
            // Mise à jour simple du statut
            selectedCredit.setStatut("Approuvé");
            creditService.updateCredit(selectedCredit);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Crédit accepté");
            loadCredits();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleRefuserCredit() {
        CreditDTO selectedCredit = tableCreditsAttente.getSelectionModel().getSelectedItem();
        if (selectedCredit == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un crédit");
            return;
        }

        try {
            // Mise à jour simple du statut
            selectedCredit.setStatut("Refusé");
            creditService.updateCredit(selectedCredit);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Crédit refusé");
            loadCredits();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleVoirDetailsCredit() {
        CreditDTO selectedCredit = tableCreditsAttente.getSelectionModel().getSelectedItem();
        if (selectedCredit == null) {
            selectedCredit = tableCreditsEnCours.getSelectionModel().getSelectedItem();
        }

        if (selectedCredit == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un crédit");
            return;
        }

        // Affichage simple des détails
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(selectedCredit.getId()).append("\n");
        details.append("Montant: ").append(selectedCredit.getMontant()).append(" FCFA\n");
        details.append("Taux: ").append(selectedCredit.getTauxInteret()).append("%\n");
        details.append("Statut: ").append(selectedCredit.getStatut());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du crédit");
        alert.setHeaderText("Informations du crédit");
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleAnnulerCredit() {
        clearForm();
    }

    @FXML
    private void handleRechercherCredit() {
        showAlert(Alert.AlertType.INFORMATION, "Recherche", "Fonctionnalité en développement");
    }

    private boolean validateCreditForm() {
        if (choiceClientCredit.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez sélectionner un client");
            return false;
        }

        if (txtMontantCredit.getText().trim().isEmpty() || txtDureeCredit.getText().trim().isEmpty() ||
                txtTauxInteret.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Tous les champs sont obligatoires");
            return false;
        }

        try {
            double montant = Double.parseDouble(txtMontantCredit.getText());
            int duree = Integer.parseInt(txtDureeCredit.getText());
            double taux = Double.parseDouble(txtTauxInteret.getText());

            if (montant <= 0 || duree <= 0 || taux < 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Les valeurs doivent être positives");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Valeurs invalides");
            return false;
        }

        return true;
    }

    private void clearForm() {
        choiceClientCredit.getSelectionModel().clearSelection();
        txtMontantCredit.clear();
        txtDureeCredit.clear();
        txtTauxInteret.clear();
    }

    private void loadData() {
        try {
            List<ClientDTO> clients = clientService.getAllClients();
            choiceClientCredit.setItems(FXCollections.observableArrayList(clients));
            loadCredits();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données");
        }
    }

    private void loadCredits() {
        try {
            List<CreditDTO> creditsAttente = creditService.getCreditsByStatut("En attente");
            tableCreditsAttente.setItems(FXCollections.observableArrayList(creditsAttente));

            List<CreditDTO> creditsApprouves = creditService.getCreditsByStatut("Approuvé");
            tableCreditsEnCours.setItems(FXCollections.observableArrayList(creditsApprouves));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les crédits");
        }
    }

    // Navigation methods - simples
    @FXML private void handleDashboard() { navigateToPage("UI_Dashboard"); }
    @FXML private void handleGestionClients() { navigateToPage("UI_Gestion_Clients"); }
    @FXML private void handleGestionComptes() { navigateToPage("UI_Gestion_Comptes"); }
    @FXML private void handleGestionTransactions() { navigateToPage("UI_Gestion_Transactions"); }
    @FXML private void handleGestionCartes() { navigateToPage("UI_Gestion_Cartes_Bancaires"); }
    @FXML private void handleServiceClient() { navigateToPage("UI_Service_Client_Rapports"); }
    @FXML private void handleGestionSupport() { navigateToPage("UI_Service_Client_Rapports"); }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToPage(String pageName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/admin/" + pageName + ".fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnValiderCredit.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Navigation impossible");
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