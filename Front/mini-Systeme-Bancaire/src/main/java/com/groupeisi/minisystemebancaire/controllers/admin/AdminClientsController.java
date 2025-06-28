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

    @FXML private TextField txtNom, txtPrenom, txtEmail, txtTelephone, txtAdresse, txtPassword, txtRechercheClient;
    @FXML private TableView<ClientDTO> tableClients;
    @FXML private TableColumn<ClientDTO, String> colNom, colPrenom, colEmail, colTelephone, colStatut;
    @FXML private Button btnAjouterClient, btnModifierClient, btnSupprimerClient, btnSuspendre, btnActiver, btnDeconnexion;


    private final ClientService clientService = new ClientService();
    private ClientDTO selectedClient;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadClients();
        setupTableSelection();
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void setupTableSelection() {
        tableClients.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedClient = newSelection;
                populateFields(newSelection);
            }
        });
    }

    private void populateFields(ClientDTO client) {
        txtNom.setText(client.getNom());
        txtPrenom.setText(client.getPrenom());
        txtEmail.setText(client.getEmail());
        txtTelephone.setText(client.getTelephone());
        txtAdresse.setText(client.getAdresse());
        txtPassword.clear(); // Ne pas afficher le mot de passe
    }

    @FXML
    private void handleAjouterClient() {
        if (!validateForm()) return;

        try {
            // ✅ CORRECTION : Utiliser setters au lieu de constructeur problématique
            ClientDTO client = new ClientDTO();
            client.setNom(txtNom.getText().trim());
            client.setPrenom(txtPrenom.getText().trim());
            client.setEmail(txtEmail.getText().trim());
            client.setTelephone(txtTelephone.getText().trim());
            client.setAdresse(txtAdresse.getText().trim());
            client.setPassword(txtPassword.getText());
            client.setStatut("Actif");

            // ✅ CORRECTION : Utiliser createClient au lieu de registerClient
            clientService.createClient(client);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Client ajouté avec succès !");
            clearForm();
            loadClients();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void handleModifierClient() {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client");
            return;
        }

        if (!validateForm()) return;

        try {
            // ✅ CORRECTION : Mettre à jour l'objet selectedClient
            selectedClient.setNom(txtNom.getText().trim());
            selectedClient.setPrenom(txtPrenom.getText().trim());
            selectedClient.setEmail(txtEmail.getText().trim());
            selectedClient.setTelephone(txtTelephone.getText().trim());
            selectedClient.setAdresse(txtAdresse.getText().trim());

            if (!txtPassword.getText().trim().isEmpty()) {
                selectedClient.setPassword(txtPassword.getText());
            }

            // ✅ CORRECTION : Utiliser updateClient avec un seul paramètre
            clientService.updateClient(selectedClient);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Client modifié avec succès !");
            clearForm();
            loadClients();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimerClient() {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce client ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                clientService.deleteClient(selectedClient.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Client supprimé avec succès");
                clearForm();
                loadClients();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSuspendre() {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client");
            return;
        }

        try {
            clientService.suspendClient(selectedClient.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Client suspendu avec succès");
            loadClients();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suspension : " + e.getMessage());
        }
    }

    @FXML
    private void handleActiver() {
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection", "Veuillez sélectionner un client");
            return;
        }

        try {
            clientService.reactivateClient(selectedClient.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Client activé avec succès");
            loadClients();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'activation : " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (txtNom.getText().trim().isEmpty() || txtPrenom.getText().trim().isEmpty() ||
                txtEmail.getText().trim().isEmpty() || txtTelephone.getText().trim().isEmpty() ||
                txtAdresse.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Tous les champs sont obligatoires");
            return false;
        }

        if (selectedClient == null && txtPassword.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le mot de passe est obligatoire pour un nouveau client");
            return false;
        }

        return true;
    }

    private void clearForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtPassword.clear();
        selectedClient = null;
        tableClients.getSelectionModel().clearSelection();
    }

    private void loadClients() {
        try {
            List<ClientDTO> clients = clientService.getAllClients();
            ObservableList<ClientDTO> clientsData = FXCollections.observableArrayList(clients);
            tableClients.setItems(clientsData);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les clients");
        }
    }

    // Navigation methods
    @FXML private void handleDashboard() { navigateToPage("UI_Dashboard"); }
    @FXML private void handleGestionComptes() { navigateToPage("UI_Gestion_Comptes"); }
    @FXML private void handleGestionTransactions() { navigateToPage("UI_Gestion_Transactions"); }
    @FXML private void handleGestionCredits() { navigateToPage("UI_Gestion_Credits"); }
    @FXML private void handleGestionCartes() { navigateToPage("UI_Gestion_Cartes_Bancaires"); }
    @FXML
    private void handleGestionSupport() {
        navigateToPage("UI_Service_Client_Rapports");
    }

    // OU si vous voulez changer le nom du handler dans le FXML :
    @FXML
    private void handleServiceClient() {
        navigateToPage("UI_Service_Client_Rapports");
    }

    @FXML
    private void handleDeconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/UI_Main.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se déconnecter");
        }
    }

    @FXML
    private void handleRechercherClient() {
        String terme = txtRechercheClient.getText().trim();
        if (terme.isEmpty()) {
            loadClients(); // Recharger tous les clients
            return;
        }

        try {
            List<ClientDTO> clients = clientService.getAllClients();
            List<ClientDTO> filtered = clients.stream()
                    .filter(client ->
                            client.getNom().toLowerCase().contains(terme.toLowerCase()) ||
                                    client.getPrenom().toLowerCase().contains(terme.toLowerCase()) ||
                                    client.getEmail().toLowerCase().contains(terme.toLowerCase()) ||
                                    String.valueOf(client.getId()).contains(terme)
                    )
                    .toList();

            tableClients.setItems(FXCollections.observableArrayList(filtered));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de recherche: " + e.getMessage());
        }
    }


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
            Stage stage = (Stage) btnAjouterClient.getScene().getWindow();
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