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
        // Table crédits en attente
        colIdCredit.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMontantCredit.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDureeCredit.setCellValueFactory(new PropertyValueFactory<>("dureeMois"));
        colTauxInteret.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
        colStatutCredit.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colClientCredit.setCellValueFactory(cellData -> {
            CreditDTO credit = cellData.getValue();
            if (credit.getClient() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        credit.getClient().getNom() + " " + credit.getClient().getPrenom()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Table crédits en cours
        colIdCreditCours.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMontantCreditCours.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMensualiteCreditCours.setCellValueFactory(new PropertyValueFactory<>("mensualite"));
        colStatutCreditCours.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colClientCreditCours.setCellValueFactory(cellData -> {
            CreditDTO credit = cellData.getValue();
            if (credit.getClient() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        credit.getClient().getNom() + " " + credit.getClient().getPrenom()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
    }

    @FXML
    private void handleValiderCredit() {
        if (!validateCreditForm()) return;

        try {
            // ✅ CORRECTION : Utiliser setters au lieu de constructeur problématique
            CreditDTO credit = new CreditDTO();
            credit.setMontant(Double.parseDouble(txtMontantCredit.getText()));
            credit.setTauxInteret(Double.parseDouble(txtTauxInteret.getText()));
            credit.setDureeMois(Integer.parseInt(txtDureeCredit.getText()));
            credit.setClientId(choiceClientCredit.getValue().getId());
            credit.setStatut("En attente");

            // Calculer la mensualité
            double mensualite = calculerMensualite(
                    credit.getMontant(),
                    credit.getTauxInteret(),
                    credit.getDureeMois()
            );
            credit.setMensualite(mensualite);

            // ✅ CORRECTION : Utiliser createCredit au lieu de demanderCredit
            creditService.createCredit(credit);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande de crédit soumise avec succès !");
            clearForm();
            loadCredits();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez saisir des valeurs numériques valides");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la soumission : " + e.getMessage());
        }
    }

    private double calculerMensualite(double montant, double tauxAnnuel, int dureeMois) {
        if (tauxAnnuel == 0) {
            return montant / dureeMois;
        }

        double tauxMensuel = tauxAnnuel / 100 / 12;
        return montant * (tauxMensuel * Math.pow(1 + tauxMensuel, dureeMois)) /
                (Math.pow(1 + tauxMensuel, dureeMois) - 1);
    }

    @FXML
    private void handleAccepterCredit() {
        CreditDTO selectedCredit = tableCreditsAttente.getSelectionModel().getSelectedItem();
        if (selectedCredit == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un crédit en attente");
            return;
        }

        try {
            creditService.accepterCredit(selectedCredit.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Crédit accepté avec succès");
            loadCredits();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'acceptation : " + e.getMessage());
        }
    }

    @FXML
    private void handleRefuserCredit() {
        CreditDTO selectedCredit = tableCreditsAttente.getSelectionModel().getSelectedItem();
        if (selectedCredit == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un crédit en attente");
            return;
        }

        try {
            creditService.refuserCredit(selectedCredit.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Crédit refusé");
            loadCredits();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du refus : " + e.getMessage());
        }
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

            if (montant < 1000) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le montant minimum est de 1000");
                return false;
            }

            if (duree < 12 || duree > 360) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La durée doit être entre 12 et 360 mois");
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir des valeurs numériques valides");
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
            // Charger les clients
            List<ClientDTO> clients = clientService.getAllClients();
            choiceClientCredit.setItems(FXCollections.observableArrayList(clients));

            loadCredits();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les données");
        }
    }

    private void loadCredits() {
        try {
            // Charger les crédits en attente
            List<CreditDTO> creditsAttente = creditService.getCreditsByStatut("En attente");
            ObservableList<CreditDTO> creditsAttenteData = FXCollections.observableArrayList(creditsAttente);
            tableCreditsAttente.setItems(creditsAttenteData);

            // Charger les crédits approuvés
            List<CreditDTO> creditsApprouves = creditService.getCreditsByStatut("Approuvé");
            ObservableList<CreditDTO> creditsApprouvesData = FXCollections.observableArrayList(creditsApprouves);
            tableCreditsEnCours.setItems(creditsApprouvesData);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les crédits");
        }
    }

    // Navigation methods
    @FXML private void handleDashboard() { navigateToPage("UI_Dashboard"); }
    @FXML private void handleGestionClients() { navigateToPage("UI_Gestion_Clients"); }
    @FXML private void handleGestionComptes() { navigateToPage("UI_Gestion_Comptes"); }
    @FXML private void handleGestionTransactions() { navigateToPage("UI_Gestion_Transactions"); }
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
            Stage stage = (Stage) btnValiderCredit.getScene().getWindow();
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