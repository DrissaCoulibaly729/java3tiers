package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import com.groupeisi.minisystemebancaire.services.TransactionService;
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

public class ClientTransactionsController {

    /*------------- Services & état ----------------------------*/
    private final TransactionService transactionService = new TransactionService();
    private Long clientId;                  // id du client connecté

    /*------------- FXML ---------------------------------------*/
    @FXML private ChoiceBox<String> transactionType;
    @FXML private TextField montantField;
    @FXML private TextField compteSourceField;
    @FXML private TextField compteDestField;
    @FXML private Button   btnTransactions; // pour la navigation
    @FXML private TableView<TransactionDTO> tableTransactions;
    @FXML private TableColumn<TransactionDTO, Long>           colId;
    @FXML private TableColumn<TransactionDTO, String>         colType;
    @FXML private TableColumn<TransactionDTO, Double>         colMontant;
    @FXML private TableColumn<TransactionDTO, LocalDateTime>  colDate;
    @FXML private TableColumn<TransactionDTO, String>         colStatut;

    /*------------- Initialisation -----------------------------*/
    @FXML
    public void initialize() {

        transactionType.getItems().setAll("Dépôt", "Retrait", "Virement");
        transactionType.setValue("Dépôt");

        colId     .setCellValueFactory(new PropertyValueFactory<>("id"));
        colType   .setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colDate   .setCellValueFactory(new PropertyValueFactory<>("date"));
        colStatut .setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    public void setClientId(Long id) {
        this.clientId = id;
        rafraichirTable();
    }

    /*------------- Actions UI --------------------------------*/
    @FXML
    public void handleTransaction() {

        try {
            String type    = transactionType.getValue();
            double montant = Double.parseDouble(montantField.getText().trim());

            Long idSource = null;
            Long idDest   = null;

            switch (type) {
                case "Dépôt":
                    if (compteDestField.getText().isBlank()) {
                        alerteErreur("Le compte destinataire est requis.");
                        return;
                    }
                    idDest = Long.parseLong(compteDestField.getText().trim());
                    break;

                case "Retrait":
                    if (compteSourceField.getText().isBlank()) {
                        alerteErreur("Le compte source est requis.");
                        return;
                    }
                    idSource = Long.parseLong(compteSourceField.getText().trim());
                    break;

                case "Virement":
                    if (compteSourceField.getText().isBlank() || compteDestField.getText().isBlank()) {
                        alerteErreur("Comptes source et destinataire requis.");
                        return;
                    }
                    idSource = Long.parseLong(compteSourceField.getText().trim());
                    idDest   = Long.parseLong(compteDestField.getText().trim());
                    break;
            }

            TransactionDTO dto = new TransactionDTO(
                    null,
                    type,
                    montant,
                    LocalDateTime.now(),
                    idSource,
                    idDest,
                    "validé"                   // minuscule pour l’API
            );

            transactionService.enregistrerTransaction(dto);
            rafraichirTable();
            viderChamps();
            alerteInfo("Succès", type + " de " + montant + " FCFA effectué !");

        } catch (NumberFormatException nfe) {
            alerteErreur("Montant ou numéro de compte invalide.");
        } catch (Exception ex) {
            alerteErreur("Erreur : " + ex.getMessage());
        }
    }

    /*------------- Table -------------------------------------*/
    private void rafraichirTable() {
        List<TransactionDTO> list = transactionService.getTransactionsByClientId(clientId);
        tableTransactions.getItems().setAll(list);
    }

    /*------------- Helpers UI --------------------------------*/
    private void viderChamps() {
        montantField.clear();
        compteSourceField.clear();
        compteDestField.clear();
    }
    private void alerteInfo(String t,String m){ alert(Alert.AlertType.INFORMATION,t,m);}
    private void alerteErreur(String m)      { alert(Alert.AlertType.ERROR,"Erreur",m); }
    private void alert(Alert.AlertType type,String titre,String msg){
        Alert a=new Alert(type); a.setTitle(titre); a.setContentText(msg); a.showAndWait();
    }

    /*------------- Navigation (exemple) -----------------------*/
    @FXML
    public void goToDashboard() { navigate("UI_Dashboard"); }
    @FXML
    private void goToCredits() {
        // Navigation vers la page des crédits
        navigate("UI_Credits");
    }

    @FXML private void goToCartes()      { navigate("UI_Cartes_Bancaires"); }
    @FXML private void goToSupport()     { navigate("UI_Service_Client");   }
    @FXML private void handleLogout()    { navigate("UI_Login");            }

    @FXML
    private void clearFields() {
        montantField.clear();
        compteSourceField.clear();
        compteDestField.clear();
    }
    private void navigate(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/com/groupeisi/minisystemebancaire/client/" + fxml + ".fxml"));
            BorderPane pane = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof ClientDashboardController c)  c.setClientId(clientId);
            if (ctrl instanceof ClientCreditsController    c) c.setClientId(clientId);
            if (ctrl instanceof ClientCartesController     c) c.setClientId(clientId);
            if (ctrl instanceof ClientSupportController    c) c.setClientId(clientId);
            if (ctrl instanceof ClientTransactionsController c) c.setClientId(clientId);

            Stage stage = (Stage) btnTransactions.getScene().getWindow();
            stage.setScene(new Scene(pane));

        } catch (IOException e) {
            alerteErreur("Impossible de charger la page : " + e.getMessage());
        }
    }
}
