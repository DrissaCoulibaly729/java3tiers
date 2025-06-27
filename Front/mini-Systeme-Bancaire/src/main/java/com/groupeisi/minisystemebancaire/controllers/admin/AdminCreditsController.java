package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.CreditDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.CreditService;
import com.groupeisi.minisystemebancaire.services.ClientService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class AdminCreditsController {
    private final CreditService creditService = new CreditService();
    private final ClientService clientService = new ClientService();

    @FXML
    private TextField txtRechercheCredit, txtMontantCredit, txtDureeCredit, txtTauxInteret;
    @FXML
    private ChoiceBox<ClientDTO> choiceClientCredit;
    @FXML
    private TableView<CreditDTO> tableCreditsAttente, tableCreditsEnCours;
    @FXML
    private TableColumn<CreditDTO, Long> colIdCredit, colIdCreditCours;
    @FXML
    private TableColumn<CreditDTO, String> colClientCredit, colClientCreditCours;
    @FXML
    private TableColumn<CreditDTO, Double> colMontantCredit, colMontantCreditCours, colMensualiteCreditCours;
    @FXML
    private TableColumn<CreditDTO, Integer> colDureeCredit;
    @FXML
    private TableColumn<CreditDTO, Double> colTauxInteret;
    @FXML
    private TableColumn<CreditDTO, String> colStatutCredit, colStatutCreditCours;
    @FXML
    private Button btnValiderCredit, btnAnnulerCredit, btnAccepterCredit, btnRefuserCredit, btnVoirDetailsCredit,btnDeconnexion;

    /**
     * ✅ Initialise le contrôleur et charge les données nécessaires.
     */
    @FXML
    public void initialize() {
        // Configurer les colonnes des tableaux
        colIdCredit.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientCredit.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        colMontantCredit.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDureeCredit.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colTauxInteret.setCellValueFactory(new PropertyValueFactory<>("tauxInteret"));
        colStatutCredit.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colIdCreditCours.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientCreditCours.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        colMontantCreditCours.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colMensualiteCreditCours.setCellValueFactory(new PropertyValueFactory<>("mensualite"));
        colStatutCreditCours.setCellValueFactory(new PropertyValueFactory<>("statut"));

        chargerClients();
        chargerCredits();
    }

    /**
     * ✅ Charge les clients dans le `ChoiceBox`
     */
    private void chargerClients() {
        List<ClientDTO> clients = clientService.getAllClients();

        // 🔍 Debug : Vérifier si les clients ont un ID
        for (ClientDTO client : clients) {
            System.out.println("Client trouvé : " + client.getNom() + " " + client.getPrenom() + ", ID: " + client.getId());
        }

        choiceClientCredit.getItems().setAll(clients);

        choiceClientCredit.setConverter(new StringConverter<>() {
            @Override
            public String toString(ClientDTO client) {
                return (client != null) ? client.getNom() + " " + client.getPrenom() : "";
            }

            @Override
            public ClientDTO fromString(String string) {
                return null;
            }
        });
    }

    /**
     * ✅ Charge les crédits en attente et en cours dans les tableaux
     */
    private void chargerCredits() {
        List<CreditDTO> creditsAttente = creditService.getCreditsByStatut("En attente");
        List<CreditDTO> creditsEnCours = creditService.getCreditsByStatut("Accepté");

        tableCreditsAttente.getItems().setAll(creditsAttente);
        tableCreditsEnCours.getItems().setAll(creditsEnCours);
    }

    /**
     * ✅ Gère la recherche d'un crédit
     */
    @FXML
    public void handleRechercherCredit() {
        String recherche = txtRechercheCredit.getText().trim();
        if (recherche.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un ID ou un montant.");
            return;
        }

        try {
            long id = Long.parseLong(recherche);
            CreditDTO credit = creditService.getCreditById(id);
            if (credit != null) {
                tableCreditsAttente.getItems().setAll(credit);
            } else {
                showAlert(Alert.AlertType.WARNING, "Aucun crédit trouvé", "Aucun crédit ne correspond à cet ID.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un ID valide.");
        }
    }

    /**
     * ✅ Gère l'ajout d'une demande de crédit
     */
    @FXML
    public void handleValiderCredit() {
        ClientDTO client = choiceClientCredit.getValue();
        double montant;
        int duree;
        double taux;

        try {
            montant = Double.parseDouble(txtMontantCredit.getText());
            duree = Integer.parseInt(txtDureeCredit.getText());
            taux = Double.parseDouble(txtTauxInteret.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs valides.");
            return;
        }

        if (client == null || montant <= 0 || duree <= 0 || taux <= 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs correctement.");
            return;
        }

        CreditDTO creditDTO = new CreditDTO(
                null,
                montant,
                taux,
                duree,
                calculerMensualite(montant, taux, duree),  // Méthode pour calculer la mensualité
                LocalDateTime.now(), // Date actuelle
                "En attente",
                client.getId()
        );

        creditService.demanderCredit(creditDTO);
        chargerCredits();

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande de crédit enregistrée.");
    }

    /**
     * ✅ Accepter une demande de crédit
     */
    @FXML
    public void handleAccepterCredit() {
        CreditDTO selectedCredit = tableCreditsAttente.getSelectionModel().getSelectedItem();
        if (selectedCredit == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une demande de crédit.");
            return;
        }

        creditService.accepterCredit(selectedCredit.getId());
        chargerCredits();
        showAlert(Alert.AlertType.INFORMATION, "Crédit accepté", "La demande de crédit a été acceptée.");
    }

    /**
     * ✅ Refuser une demande de crédit
     */
    @FXML
    public void handleRefuserCredit() {
        CreditDTO selectedCredit = tableCreditsAttente.getSelectionModel().getSelectedItem();
        if (selectedCredit == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une demande de crédit.");
            return;
        }

        creditService.refuserCredit(selectedCredit.getId());
        chargerCredits();
        showAlert(Alert.AlertType.INFORMATION, "Crédit refusé", "La demande de crédit a été refusée.");
    }

    /**
     * ✅ Voir les détails d'un crédit en cours
     */
    @FXML
    public void handleVoirDetailsCredit() {
        CreditDTO selectedCredit = tableCreditsEnCours.getSelectionModel().getSelectedItem();
        if (selectedCredit == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un crédit en cours.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Détails du crédit",
                "ID : " + selectedCredit.getId() +
                        "\nMontant : " + selectedCredit.getMontant() +
                        "\nDurée : " + selectedCredit.getDureeMois() + " mois" +
                        "\nTaux : " + selectedCredit.getTauxInteret() + "%" +
                        "\nStatut : " + selectedCredit.getStatut());
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
    private double calculerMensualite(double montant, double taux, int duree) {
        double tauxMensuel = taux / 100 / 12;
        return (montant * tauxMensuel) / (1 - Math.pow(1 + tauxMensuel, -duree));
    }

}
