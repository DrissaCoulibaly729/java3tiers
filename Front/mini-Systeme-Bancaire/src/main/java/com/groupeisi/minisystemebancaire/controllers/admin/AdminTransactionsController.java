package com.groupeisi.minisystemebancaire.controllers.admin;


import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.TransactionDTO;
import com.groupeisi.minisystemebancaire.services.CompteService;
import com.groupeisi.minisystemebancaire.services.TransactionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;

public class AdminTransactionsController {
    private final TransactionService transactionService = new TransactionService();
    private final CompteService compteService = new CompteService();

    @FXML
    private TextField txtRechercheTransaction, txtMontantTransaction;

    @FXML
    private ChoiceBox<String> choiceTypeTransaction;

    @FXML
    private ChoiceBox<CompteDTO> choiceCompteSource, choiceCompteDest;

    @FXML
    private TableView<TransactionDTO> tableTransactions;

    @FXML
    private TableColumn<TransactionDTO, Long> colIdTransaction;

    @FXML
    private TableColumn<TransactionDTO, String> colType, colStatut, colDate;

    @FXML
    private TableColumn<TransactionDTO, Double> colMontant;

    @FXML
    private TableColumn<TransactionDTO, String> colCompteSource, colCompteDest;

    @FXML
    private Button btnRechercherTransaction, btnValiderTransaction, btnAnnulerTransaction,
            btnBloquerTransaction, btnDebloquerTransaction, btnDeconnexion;

    /**
     * ✅ Initialisation des composants
     */
    @FXML
    public void initialize() {
        // Ajout des types de transaction
        choiceTypeTransaction.getItems().addAll("Dépôt", "Retrait", "Virement");

        // Initialisation des colonnes de la table
        colIdTransaction.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colCompteSource.setCellValueFactory(new PropertyValueFactory<>("compteSourceId"));
        colCompteDest.setCellValueFactory(new PropertyValueFactory<>("compteDestId"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Charger la liste des transactions
        loadTransactions();
        loadComptesDansChoiceBox();
    }

    /**
     * ✅ Charger toutes les transactions
     */
    private void loadTransactions() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();

        colCompteSource.setCellValueFactory(cellData -> {
            Long compteSourceId = cellData.getValue().getCompteSourceId();
            if (compteSourceId == null) return new SimpleStringProperty("N/A");

            CompteDTO compteSource = compteService.getCompteById(compteSourceId);
            return new SimpleStringProperty(compteSource != null ? compteSource.getNumero() : "N/A");
        });

        colCompteDest.setCellValueFactory(cellData -> {
            Long compteDestId = cellData.getValue().getCompteDestId();
            if (compteDestId == null) return new SimpleStringProperty("N/A");

            CompteDTO compteDest = compteService.getCompteById(compteDestId);
            return new SimpleStringProperty(compteDest != null ? compteDest.getNumero() : "N/A");
        });

        tableTransactions.getItems().setAll(transactions);
    }


    /**
     * ✅ Rechercher une transaction
     */
    @FXML
    public void handleRechercherTransaction() {
        String search = txtRechercheTransaction.getText();
        if (search.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un ID ou un montant.");
            return;
        }

        try {
            long id = Long.parseLong(search);
            TransactionDTO transaction = transactionService.getTransactionById(id);
            if (transaction != null) {
                tableTransactions.getItems().setAll(transaction);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune transaction trouvée.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "ID invalide.");
        }
    }

    private void loadComptesDansChoiceBox() {
        List<CompteDTO> comptes = compteService.getAllComptes();

        choiceCompteSource.getItems().setAll(comptes);
        choiceCompteDest.getItems().setAll(comptes);

        // Utiliser un StringConverter pour afficher uniquement le numéro du compte
        choiceCompteSource.setConverter(new StringConverter<>() {
            @Override
            public String toString(CompteDTO compte) {
                return (compte != null) ? compte.getNumero() : "";
            }

            @Override
            public CompteDTO fromString(String string) {
                return null; // Non utilisé
            }
        });

        choiceCompteDest.setConverter(new StringConverter<>() {
            @Override
            public String toString(CompteDTO compte) {
                return (compte != null) ? compte.getNumero() : "";
            }

            @Override
            public CompteDTO fromString(String string) {
                return null; // Non utilisé
            }
        });
    }


    /**
     * ✅ Valider une transaction (Dépôt / Retrait / Virement)
     */
    @FXML
    public void handleValiderTransaction() {
        String type = choiceTypeTransaction.getValue();
        CompteDTO compteSource = choiceCompteSource.getValue();
        CompteDTO compteDest = choiceCompteDest.getValue();
        Long compteSourceId = (compteSource != null) ? compteSource.getId() : null;
        Long compteDestId = (compteDest != null) ? compteDest.getId() : null;
        String montantText = txtMontantTransaction.getText();

        if (type == null || montantText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un type de transaction et entrer un montant valide.");
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Montant invalide.");
            return;
        }

        TransactionDTO transactionDTO = new TransactionDTO(null, type, montant,
                java.time.LocalDateTime.now(), compteSourceId, compteDestId, "Validé");

        transactionService.enregistrerTransaction(transactionDTO);
        loadTransactions();
        clearFields();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction validée avec succès.");
    }

    /**
     * ✅ Annuler une transaction
     */
    @FXML
    public void handleAnnulerTransaction() {
        TransactionDTO selectedTransaction = tableTransactions.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une transaction à annuler.");
            return;
        }

        transactionService.annulerTransaction(selectedTransaction.getId());
        loadTransactions();
        clearFields();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction annulée.");
    }

    /**
     * ✅ Bloquer une transaction suspecte
     */
    @FXML
    public void handleBloquerTransaction() {
        TransactionDTO selectedTransaction = tableTransactions.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une transaction à bloquer.");
            return;
        }

        selectedTransaction.setStatut("Bloquée");
        transactionService.updateTransaction(selectedTransaction);
        loadTransactions();

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction bloquée.");
    }

    /**
     * ✅ Débloquer une transaction
     */
    @FXML
    public void handleDebloquerTransaction() {
        TransactionDTO selectedTransaction = tableTransactions.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une transaction à débloquer.");
            return;
        }

        selectedTransaction.setStatut("Validée");
        transactionService.updateTransaction(selectedTransaction);
        loadTransactions();

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Transaction débloquée.");
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
    @FXML
    private void clearFields() {
        txtRechercheTransaction.clear();
        txtMontantTransaction.clear();
        choiceTypeTransaction.setValue(null);
        choiceCompteSource.setValue(null);
        choiceCompteDest.setValue(null);
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
     * ✅ Afficher une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
