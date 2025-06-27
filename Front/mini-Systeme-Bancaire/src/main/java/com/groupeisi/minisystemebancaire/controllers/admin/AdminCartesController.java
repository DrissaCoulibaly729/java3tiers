package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.CarteBancaireDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.CarteBancaireService;
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
import java.util.List;

public class AdminCartesController {
    private final CarteBancaireService carteService = new CarteBancaireService();
    private final ClientService clientService = new ClientService();

    @FXML
    private TextField txtRechercheCarte, txtSoldeCarte;

    @FXML
    private ChoiceBox<String> choiceTypeCarte;

    @FXML
    private ChoiceBox<ClientDTO> choiceClientCarte;

    @FXML
    private TableView<CarteBancaireDTO> tableCartes;

    @FXML
    private TableColumn<CarteBancaireDTO, String> colNumeroCarte, colTypeCarte, colCVV, colExpiration, colStatutCarte;

    @FXML
    private Button btnDemanderCarte, btnAnnulerCarte, btnBloquerCarte, btnDebloquerCarte, btnRechercherCarte,btnDeconnexion;

    /**
     * ✅ Initialise la table et charge les types de carte et clients
     */
    @FXML
    public void initialize() {
        // Ajout des types de carte disponibles
        choiceTypeCarte.getItems().addAll("Visa", "MasterCard", "American Express");

        // Chargement des clients
        chargerClients();

        // Initialisation des colonnes de la table
        colNumeroCarte.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colTypeCarte.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCVV.setCellValueFactory(new PropertyValueFactory<>("cvv"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colStatutCarte.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Chargement initial des cartes bancaires
        afficherCartes();
    }

    /**
     * ✅ Charge la liste des clients pour l'affichage dans ChoiceBox
     */
    private void chargerClients() {
        List<ClientDTO> clients = clientService.getAllClients();

        // 🔍 Debug : Vérifier si les clients ont un ID
        for (ClientDTO client : clients) {
            System.out.println("Client trouvé : " + client.getNom() + " " + client.getPrenom() + ", ID: " + client.getId());
        }

        choiceClientCarte.getItems().setAll(clients);

        choiceClientCarte.setConverter(new StringConverter<ClientDTO>() {
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
     * ✅ Affiche la liste des cartes bancaires
     */
    private void afficherCartes() {
        List<CarteBancaireDTO> cartes = carteService.getAllCartes();
        tableCartes.getItems().setAll(cartes);
    }

    /**
     * ✅ Recherche une carte par son numéro
     */
    @FXML
    public void handleRechercherCarte() {
        String numeroCarte = txtRechercheCarte.getText();
        if (numeroCarte.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un numéro de carte.");
            return;
        }

        CarteBancaireDTO carte = carteService.getCarteByNumero(numeroCarte);
        if (carte == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Carte non trouvée.");
        } else {
            tableCartes.getItems().setAll(carte);
        }
    }

    /**
     * ✅ Gère la demande d'une carte bancaire pour un client
     */
    @FXML
    public void handleDemanderCarte() {
        ClientDTO client = choiceClientCarte.getValue();
        String type = choiceTypeCarte.getValue();
        double solde;

        try {
            solde = Double.parseDouble(txtSoldeCarte.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un montant valide.");
            return;
        }

        if (client == null || type == null || solde <= 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez choisir un client, un type de carte et entrer un montant valide.");
            return;
        }

        CarteBancaireDTO carte = new CarteBancaireDTO(
                null,
                generateNumeroCarte(),
                type,
                generateCVV(),
                "12/2026",
                solde,
                "Active",
                client.getId(),
                generateCodePin()
        );

        carteService.demanderCarte(carte);
        afficherCartes(); // Rafraîchir la liste après la demande
        showAlert(Alert.AlertType.INFORMATION, "Carte créée", "La carte a été attribuée au client.");
    }

    /**
     * ✅ Gère le blocage d'une carte bancaire
     */
    @FXML
    public void handleBloquerCarte() {
        CarteBancaireDTO selectedCarte = tableCartes.getSelectionModel().getSelectedItem();
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une carte à bloquer.");
            return;
        }

        carteService.updateCarteStatut(selectedCarte.getId(), "Bloquée");
        afficherCartes();
        showAlert(Alert.AlertType.INFORMATION, "Carte bloquée", "La carte " + selectedCarte.getNumero() + " a été bloquée.");
    }

    /**
     * ✅ Gère le déblocage d'une carte bancaire
     */
    @FXML
    public void handleDebloquerCarte() {
        CarteBancaireDTO selectedCarte = tableCartes.getSelectionModel().getSelectedItem();
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une carte à débloquer.");
            return;
        }

        carteService.updateCarteStatut(selectedCarte.getId(), "Active");
        afficherCartes();
        showAlert(Alert.AlertType.INFORMATION, "Carte débloquée", "La carte " + selectedCarte.getNumero() + " a été débloquée.");
    }

    /**
     * ✅ Génère un numéro de carte aléatoire (16 chiffres)
     */
    private String generateNumeroCarte() {
        return "4000" + (long) (Math.random() * 1000000000000L);
    }

    /**
     * ✅ Génère un CVV aléatoire (3 chiffres)
     */
    private String generateCVV() {
        return String.valueOf((int) (Math.random() * 900) + 100);
    }

    /**
     * ✅ Génère un code PIN aléatoire (4 chiffres)
     */
    private String generateCodePin() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
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
