package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.TicketSupportDTO;
import com.groupeisi.minisystemebancaire.services.TicketSupportService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminSupportController {

    @FXML private TextField txtRechercheTicket;
    @FXML private TextArea txtReponse;
    @FXML private TableView<TicketSupportDTO> tableTicketsOuverts, tableTicketsResolus;
    @FXML private TableColumn<TicketSupportDTO, Long> colIdTicketOuvert, colIdTicketResolu;
    @FXML private TableColumn<TicketSupportDTO, String> colClientOuvert, colSujetOuvert, colStatutOuvert;
    @FXML private TableColumn<TicketSupportDTO, String> colClientResolu, colSujetResolu, colDateResolu;
    @FXML private Button btnRepondreTicket, btnMarquerResolu, btnGenererRapport, btnDeconnexion;

    private final TicketSupportService ticketService = new TicketSupportService();

    @FXML
    private void initialize() {
        setupTableColumns();
        loadTickets();
    }

    private void setupTableColumns() {
        // Table tickets ouverts
        colIdTicketOuvert.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSujetOuvert.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        colStatutOuvert.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colClientOuvert.setCellValueFactory(cellData -> {
            TicketSupportDTO ticket = cellData.getValue();
            if (ticket.getClient() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        ticket.getClient().getNom() + " " + ticket.getClient().getPrenom()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Table tickets résolus
        colIdTicketResolu.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSujetResolu.setCellValueFactory(new PropertyValueFactory<>("sujet"));

        colClientResolu.setCellValueFactory(cellData -> {
            TicketSupportDTO ticket = cellData.getValue();
            if (ticket.getClient() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        ticket.getClient().getNom() + " " + ticket.getClient().getPrenom()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        colDateResolu.setCellValueFactory(cellData -> {
            TicketSupportDTO ticket = cellData.getValue();
            if (ticket.getDateOuverture() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        ticket.getDateOuverture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
    }

    @FXML
    private void handleRechercherTicket() {
        String recherche = txtRechercheTicket.getText().trim();
        if (recherche.isEmpty()) {
            loadTickets();
            return;
        }

        try {
            // ✅ CORRECTION : Créer une méthode de recherche simple
            List<TicketSupportDTO> allTickets = ticketService.getAllTickets();
            List<TicketSupportDTO> filteredTickets = allTickets.stream()
                    .filter(ticket ->
                            ticket.getSujet().toLowerCase().contains(recherche.toLowerCase()) ||
                                    (ticket.getClient() != null &&
                                            (ticket.getClient().getNom().toLowerCase().contains(recherche.toLowerCase()) ||
                                                    ticket.getClient().getPrenom().toLowerCase().contains(recherche.toLowerCase())))
                    )
                    .toList();

            // Séparer les tickets ouverts et résolus
            List<TicketSupportDTO> ouverts = filteredTickets.stream()
                    .filter(ticket -> !"Résolu".equals(ticket.getStatut()))
                    .toList();
            List<TicketSupportDTO> resolus = filteredTickets.stream()
                    .filter(ticket -> "Résolu".equals(ticket.getStatut()))
                    .toList();

            tableTicketsOuverts.setItems(FXCollections.observableArrayList(ouverts));
            tableTicketsResolus.setItems(FXCollections.observableArrayList(resolus));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche : " + e.getMessage());
        }
    }

    @FXML
    private void handleRepondreTicket() {
        TicketSupportDTO selectedTicket = tableTicketsOuverts.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un ticket");
            return;
        }

        if (txtReponse.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez saisir une réponse");
            return;
        }

        try {
            ticketService.repondreTicket(selectedTicket.getId(), txtReponse.getText().trim());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réponse envoyée avec succès");
            txtReponse.clear();
            loadTickets();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'envoi : " + e.getMessage());
        }
    }

    @FXML
    private void handleMarquerResolu() {
        TicketSupportDTO selectedTicket = tableTicketsOuverts.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un ticket");
            return;
        }

        try {
            // ✅ CORRECTION : Utiliser marquerResolu au lieu de marquerTicketResolu
            ticketService.marquerResolu(selectedTicket.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ticket marqué comme résolu");
            loadTickets();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la résolution : " + e.getMessage());
        }
    }

    @FXML
    private void handleGenererRapport() {
        try {
            // ✅ CORRECTION : Créer un rapport simple
            List<TicketSupportDTO> allTickets = ticketService.getAllTickets();

            long nbOuverts = allTickets.stream().filter(t -> !"Résolu".equals(t.getStatut())).count();
            long nbResolus = allTickets.stream().filter(t -> "Résolu".equals(t.getStatut())).count();

            String rapport = String.format(
                    "📊 RAPPORT SUPPORT CLIENT\n" +
                            "=======================\n\n" +
                            "📈 Statistiques :\n" +
                            "• Total tickets : %d\n" +
                            "• Tickets ouverts : %d\n" +
                            "• Tickets résolus : %d\n" +
                            "• Taux de résolution : %.1f%%\n\n" +
                            "Rapport généré le : %s",
                    allTickets.size(),
                    nbOuverts,
                    nbResolus,
                    allTickets.size() > 0 ? (nbResolus * 100.0 / allTickets.size()) : 0,
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            // Afficher le rapport dans une nouvelle fenêtre
            Alert rapportAlert = new Alert(Alert.AlertType.INFORMATION);
            rapportAlert.setTitle("Rapport Support Client");
            rapportAlert.setHeaderText(null);
            rapportAlert.setContentText(rapport);
            rapportAlert.getDialogPane().setPrefWidth(400);
            rapportAlert.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération du rapport : " + e.getMessage());
        }
    }

    private void loadTickets() {
        try {
            List<TicketSupportDTO> allTickets = ticketService.getAllTickets();

            // Séparer les tickets ouverts et résolus
            List<TicketSupportDTO> ouverts = allTickets.stream()
                    .filter(ticket -> !"Résolu".equals(ticket.getStatut()))
                    .toList();
            List<TicketSupportDTO> resolus = allTickets.stream()
                    .filter(ticket -> "Résolu".equals(ticket.getStatut()))
                    .toList();

            tableTicketsOuverts.setItems(FXCollections.observableArrayList(ouverts));
            tableTicketsResolus.setItems(FXCollections.observableArrayList(resolus));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les tickets");
        }
    }

    // Navigation methods
    @FXML private void handleDashboard() { navigateToPage("UI_Dashboard"); }
    @FXML private void handleGestionClients() { navigateToPage("UI_Gestion_Clients"); }
    @FXML private void handleGestionComptes() { navigateToPage("UI_Gestion_Comptes"); }
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
            Stage stage = (Stage) btnRepondreTicket.getScene().getWindow();
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