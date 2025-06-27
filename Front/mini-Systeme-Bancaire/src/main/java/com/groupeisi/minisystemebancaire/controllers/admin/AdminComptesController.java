package com.groupeisi.minisystemebancaire.controllers.admin;


import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.dto.ClientDTO;
import com.groupeisi.minisystemebancaire.services.CompteService;
import com.groupeisi.minisystemebancaire.services.ClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import javafx.util.StringConverter;


public class AdminComptesController {
    private final CompteService compteService = new CompteService();
    private final ClientService clientService = new ClientService();

    @FXML
    private TextField txtRechercheCompte, txtSoldeInitial, txtMontantFrais;
    @FXML
    private ChoiceBox<String> choiceTypeCompte, choiceTypeFrais;
    @FXML
    private ChoiceBox<ClientDTO> choiceClient;
    @FXML
    private ChoiceBox<String> choiceCompteFrais;
    @FXML
    private TableView<CompteDTO> tableComptes;
    @FXML
    private TableColumn<CompteDTO, String> colNumeroCompte, colType, colStatut;
    @FXML
    private TableColumn<CompteDTO, Double> colSolde;
    @FXML
    private TableColumn<CompteDTO, String> colClient;
    @FXML
    private Button btnOuvrirCompte, btnAppliquerFrais, btnModifierCompte, btnFermerCompte,btnDeconnexion;

    /**
     * ✅ Initialise le contrôleur et charge les données nécessaires.
     */
    @FXML
    public void initialize() {
        // Configuration des types de compte disponibles
        choiceTypeCompte.getItems().addAll("Courant", "Épargne", "Entreprise");

        // Configuration des types de frais bancaires
        choiceTypeFrais.getItems().addAll("Frais mensuels", "Frais de transaction", "Autres");

        // Configuration des colonnes de la table
        colNumeroCompte.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientNom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Charger les clients et les comptes
        chargerClients();
        chargerComptes();
        loadComptesDansChoiceBox();
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

        choiceClient.getItems().setAll(clients);

        choiceClient.setConverter(new StringConverter<>() {
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
    private void chargerClients() {
        List<ClientDTO> clients = clientService.getAllClients();
        choiceClient.getItems().setAll(clients);
    }
     **/

    /**
     * ✅ Charge les comptes bancaires dans la `TableView`
     */
    private void chargerComptes() {
        List<CompteDTO> comptes = compteService.getAllComptes();

        // Associer le client à la colonne en affichant Nom + Prénom
        colClient.setCellValueFactory(cellData -> {
            Long clientId = cellData.getValue().getClientId();
            // Vérifier si clientId est nul
            if (clientId == null) {
                return new SimpleStringProperty("Inconnu");
            }
            ClientDTO client = clientService.getClientById(clientId);
            return new SimpleStringProperty(client.getNom() + " " + client.getPrenom());
        });

        tableComptes.getItems().setAll(comptes);
    }

    /**
     * ✅ Gère la création d'un nouveau compte bancaire
     */
    @FXML
    public void handleOuvrirCompte() {
        ClientDTO client = choiceClient.getValue();
        String type = choiceTypeCompte.getValue();
        double soldeInitial;

        try {
            soldeInitial = Double.parseDouble(txtSoldeInitial.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un montant valide.");
            return;
        }

        if (client == null || type == null || soldeInitial < 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un client, un type de compte et un solde valide.");
            return;
        }

        // 🔍 Vérifier si l'ID du client est bien récupéré
        System.out.println("Client sélectionné : " + client.getNom() + " " + client.getPrenom() + ", ID: " + client.getId());

        if (client.getId() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'ID du client est introuvable !");
            return;
        }

        // Création du DTO
        CompteDTO compteDTO = new CompteDTO(null, LocalDateTime.now(), generateNumeroCompte(), type, soldeInitial, client.getId(), "Actif");

        // 🔍 Vérifier avant d'envoyer au service
        System.out.println("Compte créé pour ClientID: " + compteDTO.getClientId());

        compteService.createCompte(compteDTO);
        chargerComptes();

        showAlert(Alert.AlertType.INFORMATION, "Compte créé", "Le compte a été ouvert avec succès.");
        clearFields();
    }

public void handleAnnulerCompte(){
        clearFields();
}

    /**
     * ✅ Appliquer des frais bancaires sur un compte
     */
    @FXML
    public void handleAppliquerFrais() {
        String compteNumero = choiceCompteFrais.getValue();
        String typeFrais = choiceTypeFrais.getValue();
        double montantFrais;

        try {
            montantFrais = Double.parseDouble(txtMontantFrais.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un montant valide.");
            return;
        }

        if (compteNumero == null || typeFrais == null || montantFrais <= 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un compte, un type de frais et un montant valide.");
            return;
        }

        CompteDTO compte = compteService.getCompteByNumero(compteNumero);
        if (compte == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Compte non trouvé.");
            return;
        }

        compteService.appliquerFrais(compte.getId(), montantFrais);
        chargerComptes();

        showAlert(Alert.AlertType.INFORMATION, "Frais appliqués", "Les frais ont été appliqués avec succès.");
        clearFieldsFrais();
    }

    /**
     * ✅ Fermer un compte bancaire
     */
    @FXML
    public void handleFermerCompte() {
        CompteDTO selectedCompte = tableComptes.getSelectionModel().getSelectedItem();
        if (selectedCompte == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un compte à fermer.");
            return;
        }

        compteService.fermerCompte(selectedCompte.getId());
        chargerComptes();
        showAlert(Alert.AlertType.INFORMATION, "Compte fermé", "Le compte a été fermé avec succès.");
    }

    /**
     * ✅ Modifier un compte bancaire (Ex: changer le statut)
     */
    @FXML
    public void handleModifierCompte() {
        CompteDTO selectedCompte = tableComptes.getSelectionModel().getSelectedItem();
        if (selectedCompte == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un compte à modifier.");
            return;
        }

        String nouveauStatut = selectedCompte.getStatut().equals("Actif") ? "Suspendu" : "Actif";
        selectedCompte.setStatut(nouveauStatut);
        compteService.updateCompte(selectedCompte.getId(), selectedCompte);
        chargerComptes();
        showAlert(Alert.AlertType.INFORMATION, "Compte modifié", "Le compte a été mis à jour.");
    }

    private void loadComptesDansChoiceBox() {
        List<CompteDTO> comptes = compteService.getAllComptes();
        for (CompteDTO compte : comptes) {
            choiceCompteFrais.getItems().add(compte.getNumero()); // Ajoute le numéro du compte
        }
        choiceTypeFrais.getItems().addAll("Frais de maintenance", "Frais de virement", "Frais de gestion");
    }

    /**
     * ✅ Génère un numéro de compte bancaire unique
     */
    private String generateNumeroCompte() {
        return "CB" + (long) (Math.random() * 1000000000);
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
     * ✅ Efface tous les champs après l'ajout d'un compte.
     */
    private void clearFields() {
        choiceClient.setValue(null);
        choiceTypeCompte.setValue(null);
        txtSoldeInitial.clear();
    }
    private void clearFieldsFrais() {
        choiceCompteFrais.setValue(null);
        choiceTypeFrais.setValue(null);
        txtMontantFrais.clear();
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
