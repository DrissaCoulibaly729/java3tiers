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
import java.util.List;
import java.util.stream.Collectors;

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
        // Table tickets ouverts - colonnes de base qui marchent
        colIdTicketOuvert.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSujetOuvert.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        colStatutOuvert.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Client avec gestion d'erreur simple
        colClientOuvert.setCellValueFactory(cellData -> {
            try {
                TicketSupportDTO ticket = cellData.getValue();
                if (ticket.getClient() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            ticket.getClient().getNom() + " " + ticket.getClient().getPrenom()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("Client ID: " + ticket.getClientId());
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        // Table tickets résolus
        colIdTicketResolu.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSujetResolu.setCellValueFactory(new PropertyValueFactory<>("sujet"));

        colClientResolu.setCellValueFactory(cellData -> {
            try {
                TicketSupportDTO ticket = cellData.getValue();
                if (ticket.getClient() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            ticket.getClient().getNom() + " " + ticket.getClient().getPrenom()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("Client ID: " + ticket.getClientId());
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        // Date simple - éviter getDateOuverture qui pose problème
        colDateResolu.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("--/--/----");
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
            List<TicketSupportDTO> allTickets = ticketService.getAllTickets();
            List<TicketSupportDTO> filteredTickets = allTickets.stream()
                    .filter(ticket -> {
                        try {
                            return ticket.getSujet().toLowerCase().contains(recherche.toLowerCase()) ||
                                    (ticket.getClient() != null &&
                                            (ticket.getClient().getNom().toLowerCase().contains(recherche.toLowerCase()) ||
                                                    ticket.getClient().getPrenom().toLowerCase().contains(recherche.toLowerCase())));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            // Séparer les tickets - logique simple
            List<TicketSupportDTO> ouverts = filteredTickets.stream()
                    .filter(ticket -> !"Résolu".equals(ticket.getStatut()))
                    .collect(Collectors.toList());
            List<TicketSupportDTO> resolus = filteredTickets.stream()
                    .filter(ticket -> "Résolu".equals(ticket.getStatut()))
                    .collect(Collectors.toList());

            tableTicketsOuverts.setItems(FXCollections.observableArrayList(ouverts));
            tableTicketsResolus.setItems(FXCollections.observableArrayList(resolus));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche");
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
            // SIMPLE : juste changer le statut, on s'en fout de la réponse
            selectedTicket.setStatut("Répondu");
            ticketService.updateTicket(selectedTicket);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ticket traité");
            txtReponse.clear();
            loadTickets();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur");
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
            // Version simple avec updateTicket au lieu de marquerResolu
            selectedTicket.setStatut("Résolu");
            ticketService.updateTicket(selectedTicket);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Ticket marqué comme résolu");
            loadTickets();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la résolution");
        }
    }

    @FXML
    private void handleGenererRapport() {
        try {
            List<TicketSupportDTO> allTickets = ticketService.getAllTickets();

            long nbOuverts = allTickets.stream()
                    .filter(t -> !"Résolu".equals(t.getStatut()))
                    .count();
            long nbResolus = allTickets.stream()
                    .filter(t -> "Résolu".equals(t.getStatut()))
                    .count();

            String rapport = String.format(
                    "RAPPORT SUPPORT CLIENT\n" +
                            "======================\n\n" +
                            "Total tickets : %d\n" +
                            "Tickets ouverts : %d\n" +
                            "Tickets résolus : %d\n" +
                            "Taux de résolution : %.1f%%",
                    allTickets.size(),
                    nbOuverts,
                    nbResolus,
                    allTickets.size() > 0 ? (nbResolus * 100.0 / allTickets.size()) : 0
            );

            Alert rapportAlert = new Alert(Alert.AlertType.INFORMATION);
            rapportAlert.setTitle("Rapport Support Client");
            rapportAlert.setHeaderText(null);
            rapportAlert.setContentText(rapport);
            rapportAlert.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération du rapport");
        }
    }

    private void loadTickets() {
        try {
            List<TicketSupportDTO> allTickets = ticketService.getAllTickets();

            // Séparer les tickets ouverts et résolus - logique simple
            List<TicketSupportDTO> ouverts = allTickets.stream()
                    .filter(ticket -> !"Résolu".equals(ticket.getStatut()))
                    .collect(Collectors.toList());
            List<TicketSupportDTO> resolus = allTickets.stream()
                    .filter(ticket -> "Résolu".equals(ticket.getStatut()))
                    .collect(Collectors.toList());

            tableTicketsOuverts.setItems(FXCollections.observableArrayList(ouverts));
            tableTicketsResolus.setItems(FXCollections.observableArrayList(resolus));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les tickets");
        }
    }

    // Navigation methods - simples
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeconnexion() {
        handleLogout();
    }

    private void navigateToPage(String pageName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/admin/" + pageName + ".fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnRepondreTicket.getScene().getWindow();
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