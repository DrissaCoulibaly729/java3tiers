package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.dto.TicketSupportDTO;
import com.groupeisi.minisystemebancaire.services.TicketSupportService;
import com.groupeisi.minisystemebancaire.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ClientSupportController {

    @FXML private TableView<TicketSupportDTO> tableTickets;
    @FXML private TableColumn<TicketSupportDTO, String> colSujet, colStatut, colDate;
    @FXML private TextField txtSujet;
    @FXML private TextArea txtMessage;
    @FXML private ComboBox<String> cmbCategorie;
    @FXML private Button btnEnvoyerTicket;
    @FXML private Button btnDashboard, btnTransactions, btnCredits, btnCartes, btnDeconnexion;
    @FXML private Label lblMessage;

    private final TicketSupportService ticketService = new TicketSupportService();
    private ClientDTO currentClient;

    @FXML
    public void initialize() {
        currentClient = SessionManager.getCurrentClient();
        if (currentClient == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Session expirée");
            redirectToLogin();
            return;
        }

        setupUI();
        setupTableColumns();
        loadTickets();
    }

    private void setupUI() {
        cmbCategorie.setItems(FXCollections.observableArrayList(
                "Problème technique", "Question sur compte", "Problème de transaction",
                "Réclamation", "Demande d'information", "Autre"
        ));
        cmbCategorie.setValue("Question sur compte");
    }

    private void setupTableColumns() {
        colSujet.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
    }

    private void loadTickets() {
        try {
            List<TicketSupportDTO> tickets = ticketService.getTicketsByClient(currentClient.getId());
            tableTickets.setItems(FXCollections.observableArrayList(tickets));
        } catch (Exception e) {
            showMessage("Erreur lors du chargement des tickets: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleEnvoyerTicket() {
        if (!validateForm()) return;

        try {
            TicketSupportDTO ticket = new TicketSupportDTO();
            ticket.setClientId(currentClient.getId());
            ticket.setSujet(txtSujet.getText().trim());
            ticket.setMessage(txtMessage.getText().trim());
            ticket.setCategorie(cmbCategorie.getValue());
            ticket.setStatut("Ouvert");

            ticketService.createTicket(ticket);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ticket envoyé avec succès !");
            clearForm();
            loadTickets();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'envoi : " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (txtSujet.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir un sujet");
            txtSujet.requestFocus();
            return false;
        }

        if (txtMessage.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir un message");
            txtMessage.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        txtSujet.clear();
        txtMessage.clear();
        cmbCategorie.setValue("Question sur compte");
    }

    @FXML private void goToDashboard() { navigateTo("UI_Dashboard"); }
    @FXML private void goToTransactions() { navigateTo("UI_Transactions"); }
    @FXML private void goToCredits() { navigateTo("UI_Credits"); }
    @FXML private void goToCartes() { navigateTo("UI_Cartes"); }
    @FXML private void handleLogout() { logout(); }

    private void navigateTo(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/" + page + ".fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de naviguer vers " + page);
        }
    }

    private void logout() {
        SessionManager.clearAllSessions();
        redirectToLogin();
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/UI_Login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) lblMessage.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
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
                case "error" -> "-fx-text-fill: red;";
                case "success" -> "-fx-text-fill: green;";
                default -> "";
            };
            lblMessage.setStyle(style);
        }
    }
}