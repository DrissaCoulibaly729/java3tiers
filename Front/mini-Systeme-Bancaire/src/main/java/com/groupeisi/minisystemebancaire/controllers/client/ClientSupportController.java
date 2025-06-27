package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.TicketSupportDTO;
import com.groupeisi.minisystemebancaire.services.TicketSupportService;
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

public class ClientSupportController {
    private final TicketSupportService ticketSupportService = new TicketSupportService();
    private Long clientId; // ID du client connecté

    @FXML
    private TextField txtSujet;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TableView<TicketSupportDTO> tableReclamations;

    @FXML
    private TableColumn<TicketSupportDTO, Long> colId;

    @FXML
    private TableColumn<TicketSupportDTO, String> colSujet, colStatut, colDate;

    @FXML
    private Button btnEnvoyerReclamation, btnAnnulerReclamation, btnVoirDetails;

    @FXML
    private Button btnDashboard, btnTransactions, btnCredits, btnCartes, btnSupport, btnDeconnexion;

    /**
     * ✅ Initialisation de l'interface : configuration de la table des réclamations.
     */
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSujet.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateSoumission"));
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
        loadReclamations(clientId);
    }

    /**
     * ✅ Charge les réclamations du client connecté.
     */
    public void loadReclamations(Long clientId) {
        this.clientId = clientId;
        afficherReclamations();
    }

    /**
     * ✅ Affiche la liste des réclamations du client.
     */
    private void afficherReclamations() {
        List<TicketSupportDTO> tickets = ticketSupportService.getTicketsByClient(clientId);
        tableReclamations.getItems().clear();
        tableReclamations.getItems().addAll(tickets);
    }

    /**
     * ✅ Soumet une nouvelle réclamation.
     */
    @FXML
    public void handleEnvoyerReclamation() {
        String sujet = txtSujet.getText().trim();
        String description = txtDescription.getText().trim();

        if (sujet.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        TicketSupportDTO ticket = new TicketSupportDTO(
                null,
                sujet,
                description,
                LocalDateTime.now(),
                "Ouvert",
                null,
                clientId,
                null
        );

        ticketSupportService.soumettreTicket(ticket);
        afficherReclamations();
        clearFields();

        showAlert(Alert.AlertType.INFORMATION, "Réclamation soumise", "Votre réclamation a été envoyée avec succès !");
    }

    /**
     * ✅ Affiche les détails d'une réclamation sélectionnée.
     */
    @FXML
    public void handleVoirDetails() {
        TicketSupportDTO selectedTicket = tableReclamations.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une réclamation.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Détails de la réclamation",
                "Sujet: " + selectedTicket.getSujet() +
                        "\nStatut: " + selectedTicket.getStatut() +
                        "\nDate: " + selectedTicket.getDateOuverture() +
                        "\n\nDescription:\n" + selectedTicket.getDescription());
    }

    /**
     * ✅ Nettoie les champs du formulaire.
     */
    @FXML
    public void clearFields() {
        txtSujet.clear();
        txtDescription.clear();
    }

    /**
     * ✅ Gère la navigation entre les interfaces.
     */
    @FXML
    public void handleNavigation(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/" + page + ".fxml"));
            BorderPane pane = loader.load();

            Object controller = loader.getController();
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

            Stage stage = (Stage) btnSupport.getScene().getWindow();
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
     * ✅ Affiche une boîte de dialogue d'alerte.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
