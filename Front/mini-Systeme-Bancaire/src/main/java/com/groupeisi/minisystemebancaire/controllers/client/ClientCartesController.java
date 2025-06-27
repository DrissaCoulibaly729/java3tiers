package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.dto.CarteBancaireDTO;
import com.groupeisi.minisystemebancaire.dto.CompteDTO;
import com.groupeisi.minisystemebancaire.services.CarteBancaireService;
import com.groupeisi.minisystemebancaire.services.CompteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ClientCartesController {
    private final CarteBancaireService carteService = new CarteBancaireService();
    private Long clientId; // ID du client connecté

    @FXML
    private ChoiceBox<String> choiceTypeCarte;

    @FXML
    private ComboBox<CompteDTO> comboComptes;

    @FXML
    private TextField txtSoldeCarte;

    @FXML
    private TableView<CarteBancaireDTO> tableCartes;

    @FXML
    private TableColumn<CarteBancaireDTO, String> colNumero, colType, colCVV, colExpiration, colStatut;

    @FXML
    private Button btnDemanderCarte, btnAnnulerCarte, btnBloquerCarte, btnDebloquerCarte,
            btnDashboard, btnTransactions, btnCredits, btnCartes, btnSupport, btnDeconnexion;
    private final CompteService compteService = new CompteService();

    /**
     * ✅ Initialise la table des cartes et le choix des types de carte
     */
    @FXML
    public void initialize() {

        // Initialiser le ComboBox des comptes
        comboComptes.setCellFactory(param -> new ListCell<CompteDTO>() {
            @Override
            protected void updateItem(CompteDTO compte, boolean empty) {
                super.updateItem(compte, empty);
                setText(empty || compte == null ? "" : compte.getNumero() + " - " + compte.getSolde() + " FCFA");
            }
        });

        comboComptes.setButtonCell(new ListCell<CompteDTO>() {
            @Override
            protected void updateItem(CompteDTO compte, boolean empty) {
                super.updateItem(compte, empty);
                setText(empty || compte == null ? "" : compte.getNumero() + " - " + compte.getSolde() + " FCFA");
            }
        });

        if (choiceTypeCarte != null) {
            choiceTypeCarte.getItems().addAll("Visa", "MasterCard", "American Express");
            choiceTypeCarte.setValue("Visa"); // Valeur par défaut
        }

        // Initialisation des colonnes de la table
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCVV.setCellValueFactory(new PropertyValueFactory<>("cvv"));
        colExpiration.setCellValueFactory(new PropertyValueFactory<>("dateExpiration"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    /**
     * ✅ Définit l'ID du client et charge ses cartes
     */
    public void setClientId(Long clientId) {
        this.clientId = clientId;
        chargerComptesClient();
        afficherCartes();
    }

    /**
     * ✅ Affiche la liste des cartes bancaires du client
     */
    private void afficherCartes() {
        if (clientId == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Client non identifié !");
            return;
        }

        List<CarteBancaireDTO> cartes = carteService.getCartesByClient(clientId);
        System.out.println("Cartes récupérées: " + cartes.size()); // Debugging

        tableCartes.getItems().clear();
        tableCartes.getItems().addAll(cartes);
    }

    private void chargerComptesClient() {
        List<CompteDTO> comptes = compteService.getComptesByClientId(clientId);
        comboComptes.getItems().setAll(comptes);

        if(!comptes.isEmpty()) {
            comboComptes.getSelectionModel().selectFirst();
        }
    }


    /**
     * ✅ Gère la demande d'une carte bancaire
     */
    @FXML
    public void handleDemanderCarte() {
        try {
            CompteDTO compteSelectionne = comboComptes.getSelectionModel().getSelectedItem();
            String type = choiceTypeCarte.getValue();
            double solde = Double.parseDouble(txtSoldeCarte.getText().trim());

            if (type == null || solde <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez choisir un type de carte et entrer un solde valide.");
                return;
            }

            CarteBancaireDTO carte = new CarteBancaireDTO(
                    null,                          // ID (généré automatiquement)
                    generateNumeroCarte(),         // Numéro de la carte
                    type,                          // Type (Visa, MasterCard)
                    generateCVV(),                 // CVV (3 chiffres)
                    "12/2026",                     // Date d'expiration
                    solde,                         // Solde de la carte
                    "Active",                      // Statut (Active par défaut)
                    compteSelectionne.getId(),                      // ID du compte associé
                    generateCodePin()              // Code PIN (aléatoire)
            );

            carteService.demanderCarte(carte);
            afficherCartes(); // Rafraîchir la liste après la demande

            showAlert(Alert.AlertType.INFORMATION, "Carte demandée", "Votre carte " + type + " a été créée.");
            txtSoldeCarte.clear(); // Nettoyer le champ après la demande
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un montant valide.");
        }
    }

    /**
     * ✅ Gère le blocage d’une carte bancaire
     */
    @FXML
    public void handleBloquerCarte() {
        CarteBancaireDTO selectedCarte = tableCartes.getSelectionModel().getSelectedItem();
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une carte à bloquer.");
            return;
        }

        selectedCarte.setStatut("Bloquée");
        carteService.updateCarte(selectedCarte);
        afficherCartes();
        showAlert(Alert.AlertType.INFORMATION, "Carte bloquée", "La carte " + selectedCarte.getNumero() + " a été bloquée.");
    }

    /**
     * ✅ Gère le déblocage d’une carte bancaire
     */
    @FXML
    public void handleDebloquerCarte() {
        CarteBancaireDTO selectedCarte = tableCartes.getSelectionModel().getSelectedItem();
        if (selectedCarte == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner une carte à débloquer.");
            return;
        }

        selectedCarte.setStatut("Active");
        carteService.updateCarte(selectedCarte);
        afficherCartes();
        showAlert(Alert.AlertType.INFORMATION, "Carte débloquée", "La carte " + selectedCarte.getNumero() + " a été débloquée.");
    }

    /**
     * ✅ Navigation entre les interfaces avec passage de l'ID du client
     */
    @FXML
    public void handleNavigation(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/groupeisi/minisystemebancaire/client/" + page + ".fxml"));
            BorderPane pane = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ClientTransactionsController) {
                ((ClientTransactionsController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCreditsController) {
                ((ClientCreditsController) controller).setClientId(clientId);
            } else if (controller instanceof ClientDashboardController) {
                ((ClientDashboardController) controller).setClientId(clientId);
            } else if (controller instanceof ClientCartesController) {
                ((ClientCartesController) controller).setClientId(clientId);
            } else if (controller instanceof ClientSupportController) {
                ((ClientSupportController) controller).setClientId(clientId);
            }

            Stage stage = (Stage) btnCartes.getScene().getWindow();
            stage.setScene(new Scene(pane));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page.");
        }
    }

    /**
     * ✅ Gestion des boutons de navigation
     */
    @FXML
    public void goToDashboard() { handleNavigation("UI_Dashboard"); }
    @FXML
    public void goToTransactions() { handleNavigation("UI_Transactions"); }
    @FXML
    public void goToCredits() { handleNavigation("UI_Credits"); }
    @FXML
    public void goToCartes() { handleNavigation("UI_Cartes_Bancaires"); }
    @FXML
    public void goToSupport() { handleNavigation("UI_Service_Client"); }
    @FXML
    public void handleLogout() { handleNavigation("UI_Login"); }

    /**
     * ✅ Génère un numéro de carte aléatoire (16 chiffres)
     */
    private String generateNumeroCarte() {
        return "4000" + (long) (Math.random() * 1000000000000L);
    }

    private String generateCVV() {
        return String.valueOf((int) (Math.random() * 900) + 100);
    }

    private String generateCodePin() {
        return String.valueOf((int) (Math.random() * 9000) + 1000);
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
