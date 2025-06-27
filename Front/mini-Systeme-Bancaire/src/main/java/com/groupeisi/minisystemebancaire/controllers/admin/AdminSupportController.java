package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.TicketSupportDTO;
import com.groupeisi.minisystemebancaire.services.TicketSupportService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AdminSupportController {
    private final TicketSupportService ticketService = new TicketSupportService();

    @FXML
    private TextField txtRechercheTicket;
    @FXML
    private ChoiceBox<String> choiceTypeRapport, choicePeriodeRapport;
    @FXML
    private TableView<TicketSupportDTO> tableTickets;
    @FXML
    private TableColumn<TicketSupportDTO, Long> colIdTicket;
    @FXML
    private TableColumn<TicketSupportDTO, String> colClientTicket, colSujetTicket, colStatutTicket;
    @FXML
    private Button btnRepondreTicket, btnResoudreTicket, btnGenererPdf, btnGenererExcel,btnDeconnexion;

    /**
     * ✅ Initialise le contrôleur et charge les tickets
     */
    @FXML
    public void initialize() {
        // Configurer les colonnes de la table
        colIdTicket.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientTicket.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        colSujetTicket.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        colStatutTicket.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Charger les tickets en attente
        chargerTickets();

        // Remplir les choix pour la génération de rapports
        choiceTypeRapport.getItems().addAll("Tickets support", "Transactions", "Comptes", "Clients");
        choicePeriodeRapport.getItems().addAll("Dernière semaine", "Dernier mois", "Dernière année");
    }

    /**
     * ✅ Charge les tickets dans la table
     */
    private void chargerTickets() {
        List<TicketSupportDTO> tickets = ticketService.getAllTickets();
        tableTickets.getItems().setAll(tickets);
    }

    /**
     * ✅ Recherche un ticket par ID ou sujet
     */
    @FXML
    public void handleRechercherTicket() {
        String recherche = txtRechercheTicket.getText().trim();
        if (recherche.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un ID ou un sujet.");
            return;
        }

        List<TicketSupportDTO> tickets = ticketService.rechercherTicket(recherche);
        if (tickets.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun ticket trouvé", "Aucun ticket ne correspond à cette recherche.");
        } else {
            tableTickets.getItems().setAll(tickets);
        }
    }

    /**
     * ✅ Répondre à un ticket sélectionné
     */
    @FXML
    public void handleRepondreTicket() {
        TicketSupportDTO selectedTicket = tableTickets.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un ticket à répondre.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Répondre au Ticket");
        dialog.setHeaderText("Réponse au client");
        dialog.setContentText("Entrez votre réponse :");

        dialog.showAndWait().ifPresent(reponse -> {
            ticketService.repondreTicket(selectedTicket.getId(), reponse);
            showAlert(Alert.AlertType.INFORMATION, "Réponse envoyée", "Votre réponse a été envoyée au client.");
        });
    }

    /**
     * ✅ Marquer un ticket comme résolu
     */
    @FXML
    public void handleResoudreTicket() {
        TicketSupportDTO selectedTicket = tableTickets.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un ticket à marquer comme résolu.");
            return;
        }

        ticketService.marquerTicketResolu(selectedTicket.getId());
        chargerTickets();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Le ticket a été marqué comme résolu.");
    }

    /**
     * ✅ Générer un rapport PDF ou Excel
     */
    @FXML
    public void handleGenererRapport() {
        String typeRapport = choiceTypeRapport.getValue();
        String periode = choicePeriodeRapport.getValue();

        if (typeRapport == null || periode == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un type et une période de rapport.");
            return;
        }

        ticketService.genererRapport(typeRapport, periode);
        showAlert(Alert.AlertType.INFORMATION, "Rapport généré", "Le rapport " + typeRapport + " a été généré pour " + periode);
    }

    /**
     * ✅ Changer de vue en fermant la fenêtre actuelle
     */
    private void changerDeVue(ActionEvent event, String fichierFXML) {
        try {
            // Fermer la fenêtre actuelle
            Stage stageActuel = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stageActuel.close();

            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fichierFXML));
            Scene scene = new Scene(loader.load());
            Stage nouveauStage = new Stage();
            nouveauStage.setTitle("Mini Système Bancaire");
            nouveauStage.setScene(scene);
            nouveauStage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue : " + fichierFXML);
            e.printStackTrace();
        }
    }


    /**
     * ✅ Navigation vers Gestion des Clients
     */
    @FXML
    public void handleGestionClients(ActionEvent event) {
        changerDeVue(event, "/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Clients.fxml");
    }

    /**
     * ✅ Navigation vers Gestion des Comptes
     */
    @FXML
    public void handleGestionComptes(ActionEvent event) {
        changerDeVue(event, "/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Comptes_Bancaires.fxml");
    }

    /**
     * ✅ Navigation vers Gestion des Transactions
     */
    @FXML
    public void handleGestionTransactions(ActionEvent event) {
        changerDeVue(event, "/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Transactions.fxml");
    }

    /**
     * ✅ Navigation vers Gestion des Crédits
     */
    @FXML
    public void handleGestionCredits(ActionEvent event) {
        changerDeVue(event, "/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Credits.fxml");
    }

    /**
     * ✅ Navigation vers Gestion des Cartes Bancaires
     */
    @FXML
    public void handleGestionCartes(ActionEvent event) {
        changerDeVue(event, "/com/groupeisi/minisystemebancaire/admin/UI_Gestion_Cartes_Bancaires.fxml");
    }

    /**
     * ✅ Navigation vers Service Client & Rapports
     */
    @FXML
    public void handleGestionSupport(ActionEvent event) {
        changerDeVue(event, "/com/groupeisi/minisystemebancaire/admin/UI_Service_Client_Rapports.fxml");
    }

    /**
     * ✅ Navigation vers le Dashboard
     */
    @FXML
    public void handleDashboard(ActionEvent event) {
        changerDeVue(event, "/com/groupeisi/minisystemebancaire/admin/UI_Dashboard.fxml");
    }

    @FXML
    public void handleDeconnexion(ActionEvent event) {
        Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
        stage.close();
    }

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
