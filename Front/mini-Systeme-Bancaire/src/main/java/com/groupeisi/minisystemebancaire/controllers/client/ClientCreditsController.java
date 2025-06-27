package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.CreditDTO;
import com.groupeisi.minisystemebancaire.services.CreditService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ClientCreditsController {
    private final CreditService creditService = new CreditService();

    private Long clientId; // ID du client connecté

    @FXML
    private TextField txtMontantCredit, txtDureeCredit, txtTauxInteret;

    @FXML
    private TableView<CreditDTO> tableCredits;

    @FXML
    private TableColumn<CreditDTO, Long> colId;

    @FXML
    private TableColumn<CreditDTO, Double> colMontant;

    @FXML
    private TableColumn<CreditDTO, Integer> colDuree;

    @FXML
    private TableColumn<CreditDTO, Double> colMensualite;

    @FXML
    private TableColumn<CreditDTO, String> colStatut;

    @FXML
    private Button btnDemanderCredit, btnAnnulerCredit, btnDashboard, btnTransactions, btnCredits, btnCartes, btnSupport, btnDeconnexion;

    /**
     * ✅ Initialise la table des crédits
     */
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMois"));
        colMensualite.setCellValueFactory(new PropertyValueFactory<>("mensualite"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
        loadCredits();
    }

    /**
     * ✅ Charge les crédits du client
     */
    public void loadCredits() {
        List<CreditDTO> credits = creditService.getCreditsByClient(clientId);
        tableCredits.getItems().setAll(credits);
    }

    /**
     * ✅ Gère la demande d’un crédit
     */
    @FXML
    public void handleDemanderCredit() {
        try {
            double montant = Double.parseDouble(txtMontantCredit.getText());
            int dureeMois = Integer.parseInt(txtDureeCredit.getText());
            double tauxInteret = Double.parseDouble(txtTauxInteret.getText());

            if (montant <= 0 || dureeMois <= 0 || tauxInteret < 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs valides.");
                return;
            }

            // Calcul de la mensualité simple : (Montant * (1 + Taux)) / Durée
            double mensualite = (montant * (1 + (tauxInteret / 100))) / dureeMois;

            CreditDTO credit = new CreditDTO(
                    null,
                    montant,
                    tauxInteret,
                    dureeMois,
                    mensualite,
                    LocalDateTime.now(),
                    "En attente",
                    clientId
            );

            creditService.demanderCredit(credit);
            loadCredits(); // Rafraîchir la liste des crédits

            showAlert(Alert.AlertType.INFORMATION, "Demande envoyée", "Votre demande de crédit de " + montant + " FCFA a été soumise.");
            clearFields();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs numériques valides.");
        }
    }

    @FXML
    public void clearFields() {
        txtMontantCredit.clear();
        txtDureeCredit.clear();
        txtTauxInteret.clear();
    }

    @FXML
    public void handleNavigation(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/" + page + ".fxml"));
            BorderPane pane = loader.load();

            // Récupérer le contrôleur de la nouvelle page
            Object controller = loader.getController();

            // Vérifier si le contrôleur peut recevoir l'ID du client
            if (controller instanceof ClientTransactionsController) {
                ((ClientTransactionsController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCreditsController) {
                ((ClientCreditsController) controller).setClientId(clientId);
            } else if (controller instanceof ClientDashboardController) {
                ((ClientDashboardController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCartesController) {
                ((ClientCartesController) controller).setClientId(clientId);
            } else if (controller instanceof ClientSupportController) {
                ((ClientSupportController) controller).setClientId(clientId);
            }

            // Afficher la nouvelle scène
            Stage stage = (Stage) btnCredits.getScene().getWindow();
            stage.setScene(new Scene(pane));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page.");
        }
    }

    /**
     * ✅ Gestion des boutons de navigation
     */
    @FXML
    public void goToDashboard() { handleNavigation("UI_Dashboard"); }
    @FXML
    public void goToTransactions() { handleNavigation("UI_Transactions"); }
    @FXML
    public void goToCredits() { handleNavigation("UI_Credits"); }
    @FXML
    public void goToCartes() { handleNavigation("UI_Cartes_Bancaires"); }
    @FXML
    public void goToSupport() { handleNavigation("UI_Service_Client"); }
    @FXML
    public void handleLogout() { handleNavigation("UI_Login"); }

    /**
     * ✅ Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
