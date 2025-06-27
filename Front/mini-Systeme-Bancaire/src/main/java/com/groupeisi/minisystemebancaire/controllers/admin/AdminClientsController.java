package com.groupeisi.minisystemebancaire.controllers.admin;

import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.ClientService;
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

public class AdminClientsController {
    private final ClientService clientService = new ClientService();
    private ObservableList<ClientDTO> clientList = FXCollections.observableArrayList();
    @FXML
    private TextField txtRechercheClient, txtNom, txtPrenom, txtEmail, txtTelephone, txtAdresse;

    @FXML
    private ChoiceBox<String> choiceStatut;

    @FXML
    private TableView<ClientDTO> tableClients;

    @FXML
    private TableColumn<ClientDTO, Long> colId;

    @FXML
    private TableColumn<ClientDTO, String> colNom, colPrenom, colStatut;

    @FXML
    private Button btnRechercherClient, btnAjouterClient, btnAnnulerClient, btnModifierClient, btnSuspendreClient, btnReactiverClient, btnDeconnexion;

    /**
     * ✅ Initialise la table des clients
     */
    @FXML
    public void initialize() {

        // Vérification que TableView et les colonnes ne sont pas nulles
        assert tableClients != null : "TableView non initialisée !";
        assert colId != null : "Colonne ID non initialisée !";
        assert colNom != null : "Colonne Nom non initialisée !";
        assert colPrenom != null : "Colonne Prénom non initialisée !";
        assert colStatut != null : "Colonne Statut non initialisée !";

        // Définition des colonnes de la table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));


        // Ajout des options de statut
        choiceStatut.getItems().addAll("Actif", "Suspendu");

        // Charger la liste des clients au démarrage
        afficherClients();
    }

    /**
     * ✅ Charge tous les clients
     */
    private void afficherClients() {
        List<ClientDTO> clients = clientService.getAllClients();

        if (clients == null || clients.isEmpty()) {
            System.out.println("Aucun client trouvé !");
            return;
        }

        System.out.println("Clients récupérés : " + clients); // Debugging

        clientList.setAll(clients); // Mettre à jour la liste observable
        tableClients.setItems(clientList); // Appliquer la liste à la TableView
        tableClients.refresh(); // Rafraîchir l'affichage
    }
    private void loadClients() {
        try {
            List<ClientDTO> clients = clientService.getAllClients();
            clientList.clear();
            clientList.addAll(clients);
            tableClients.setItems(clientList);
        } catch (Exception e) {
            System.out.println("Aucun client trouvé !");
        }
    }


    /**
     * ✅ Recherche un client par ID
     */
    @FXML
    public void handleRechercherClient() {
        String recherche = txtRechercheClient.getText().trim();
        if (recherche.isEmpty()) {
            afficherClients();
            return;
        }

        try {
            Long clientId = Long.parseLong(recherche);
            ClientDTO client = clientService.getClientById(clientId);
            tableClients.getItems().setAll(client);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un ID valide.");
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    /**
     * ✅ Ajoute un nouveau client
     */
    @FXML
    public void handleAjouterClient() {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String adresse = txtAdresse.getText().trim();
        String statut = choiceStatut.getValue();
        String password = "123456"; // ⚠️ Mot de passe temporaire, à changer après inscription

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || telephone.isEmpty() || adresse.isEmpty() || statut == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        ClientDTO clientDTO = new ClientDTO(null, nom, prenom, email, telephone, adresse, statut, password);
        clientService.registerClient(clientDTO);
        afficherClients();

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Client ajouté avec succès !");
        clearForm();
    }


    /**
     * ✅ Modifier un client sélectionné
     */
    @FXML
    public void handleModifierClient() {
        ClientDTO selectedClient = tableClients.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un client.");
            return;
        }

        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String telephone = txtTelephone.getText().trim();
        String adresse = txtAdresse.getText().trim();
        String statut = choiceStatut.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || telephone.isEmpty() || adresse.isEmpty() || statut == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        selectedClient.setNom(nom);
        selectedClient.setPrenom(prenom);
        selectedClient.setEmail(email);
        selectedClient.setTelephone(telephone);
        selectedClient.setAdresse(adresse);
        selectedClient.setStatut(statut);

        clientService.updateClient(selectedClient.getId(), selectedClient);
        afficherClients();

        showAlert(Alert.AlertType.INFORMATION, "Succès", "Client modifié avec succès !");
    }

    /**
     * ✅ Suspendre un client
     */
    @FXML
    public void handleSuspendreClient() {
        ClientDTO selectedClient = tableClients.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un client.");
            return;
        }

        clientService.suspendClient(selectedClient.getId());
        afficherClients();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Client suspendu avec succès !");
    }

    /**
     * ✅ Réactiver un client
     */
    @FXML
    public void handleReactiverClient() {
        ClientDTO selectedClient = tableClients.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un client.");
            return;
        }

        clientService.reactivateClient(selectedClient.getId());
        afficherClients();
        showAlert(Alert.AlertType.INFORMATION, "Succès", "Client réactivé avec succès !");
    }

    /**
     * ✅ Annuler la saisie
     */
    @FXML
    public void handleAnnulerClient() {
        clearForm();
    }

    /**
     * ✅ Efface le formulaire
     */
    private void clearForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        choiceStatut.setValue(null);
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
