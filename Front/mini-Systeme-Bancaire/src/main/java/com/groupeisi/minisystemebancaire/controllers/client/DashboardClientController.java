package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.*;
import com.groupeisi.minisystemebancaire.services.*;
import com.groupeisi.minisystemebancaire.utils.CurrencyFormatter;
import com.groupeisi.minisystemebancaire.utils.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardClientController implements Initializable {

    // Labels d'information
    @FXML private Label welcomeLabel;
    @FXML private Label soldeLabel;
    @FXML private Label nombreTransactionsLabel;

    // Table des comptes
    @FXML private TableView<CompteDTo> comptesTable;
    @FXML private TableColumn<CompteDTo, String> accountNumberCol;
    @FXML private TableColumn<CompteDTo, String> accountTypeCol;
    @FXML private TableColumn<CompteDTo, BigDecimal> accountBalanceCol;

    // Table des transactions
    @FXML private TableView<TransactionDTo> dernieresTransactionsTable;
    @FXML private TableColumn<TransactionDTo, String> transactionTypeCol;
    @FXML private TableColumn<TransactionDTo, BigDecimal> transactionMontantCol;
    @FXML private TableColumn<TransactionDTo, String> transactionDateCol;
    @FXML private TableColumn<TransactionDTo, String> transactionStatutCol;

    // Boutons d'action (changés de JFXButton vers Button)
    @FXML private Button virementsBtn;
    @FXML private Button cartesBtn;
    @FXML private Button creditsBtn;
    @FXML private Button supportBtn;
    @FXML private Button deconnexionBtn;

    // Variables privées
    private ClientDTo clientConnecte;
    private Long clientId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialisation du dashboard client...");

        // Vérifier si l'utilisateur est connecté
        clientId = ApiConfig.getCurrentUserId();
        if (clientId == null) {
            System.out.println("Aucun utilisateur connecté, redirection vers login");
            // Rediriger vers la connexion si pas connecté
            Platform.runLater(() -> {
                WindowManager.showError("Erreur", "Session expirée", "Veuillez vous reconnecter.");
                try {
                    WindowManager.closeWindow();
                    WindowManager.openWindow("/com/groupeisi/minisystemebancaire/connexion.fxml", "Connexion");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        System.out.println("Client ID: " + clientId);

        // Configurer les composants
        setupTables();
        setupButtons();
        loadDashboardData();
    }

    private void setupTables() {
        System.out.println("Configuration des tables...");

        // Configuration de la table des comptes
        if (accountNumberCol != null) {
            accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("numeroCompte"));
        }
        if (accountTypeCol != null) {
            accountTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (accountBalanceCol != null) {
            accountBalanceCol.setCellValueFactory(new PropertyValueFactory<>("solde"));
            accountBalanceCol.setCellFactory(column -> new TableCell<CompteDTo, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(CurrencyFormatter.format(item.doubleValue()));
                    }
                }
            });
        }

        // Configuration de la table des transactions
        if (transactionTypeCol != null) {
            transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (transactionMontantCol != null) {
            transactionMontantCol.setCellValueFactory(new PropertyValueFactory<>("montant"));
            transactionMontantCol.setCellFactory(column -> new TableCell<TransactionDTo, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(CurrencyFormatter.format(item.doubleValue()));
                    }
                }
            });
        }
        if (transactionDateCol != null) {
            transactionDateCol.setCellValueFactory(new PropertyValueFactory<>("dateTransaction"));
        }
        if (transactionStatutCol != null) {
            transactionStatutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        }
    }

    private void setupButtons() {
        System.out.println("Configuration des boutons...");

        if (virementsBtn != null) {
            virementsBtn.setOnAction(e -> ouvrirVirements());
        }
        if (cartesBtn != null) {
            cartesBtn.setOnAction(e -> ouvrirCartes());
        }
        if (creditsBtn != null) {
            creditsBtn.setOnAction(e -> ouvrirCredits());
        }
        if (supportBtn != null) {
            supportBtn.setOnAction(e -> ouvrirSupport());
        }
        if (deconnexionBtn != null) {
            deconnexionBtn.setOnAction(e -> seDeconnecter());
        }
    }

    private void loadDashboardData() {
        System.out.println("Chargement des données du dashboard...");

        // Afficher un message de bienvenue par défaut
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue dans votre espace client !");
        }
        if (soldeLabel != null) {
            soldeLabel.setText("Solde total: Chargement...");
        }
        if (nombreTransactionsLabel != null) {
            nombreTransactionsLabel.setText("Transactions ce mois: Chargement...");
        }

        // Charger les comptes du client
        loadComptes();

        // Charger les transactions récentes
        loadTransactions();
    }

    private void loadComptes() {
        // Simuler des données pour le moment
        Platform.runLater(() -> {
            if (soldeLabel != null) {
                soldeLabel.setText("Solde total: 1 250,75 €");
            }

            // TODO: Remplacer par un vrai appel API
            // CompteService.getComptesByClient(clientId)
            //     .thenAccept(comptes -> {
            //         Platform.runLater(() -> {
            //             if (comptesTable != null) {
            //                 comptesTable.setItems(FXCollections.observableArrayList(comptes));
            //             }
            //         });
            //     });
        });
    }

    private void loadTransactions() {
        // Simuler des données pour le moment
        Platform.runLater(() -> {
            if (nombreTransactionsLabel != null) {
                nombreTransactionsLabel.setText("Transactions ce mois: 8");
            }

            // TODO: Remplacer par un vrai appel API
            // TransactionService.getTransactionsByClient(clientId)
            //     .thenAccept(transactions -> {
            //         Platform.runLater(() -> {
            //             if (dernieresTransactionsTable != null) {
            //                 dernieresTransactionsTable.setItems(FXCollections.observableArrayList(transactions));
            //             }
            //         });
            //     });
        });
    }

    // Actions des boutons

    @FXML
    private void ouvrirVirements() {
        System.out.println("Ouverture de la page virements");
        try {
            WindowManager.openWindow("/com/groupeisi/minisystemebancaire/client/virements.fxml", "Virements");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture des virements", e);
        }
    }

    @FXML
    private void ouvrirCartes() {
        System.out.println("Ouverture de la page cartes bancaires");
        try {
            WindowManager.openWindow("/com/groupeisi/minisystemebancaire/client/cartes.fxml", "Mes Cartes");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture des cartes", e);
        }
    }

    @FXML
    private void ouvrirCredits() {
        System.out.println("Ouverture de la page crédits");
        try {
            WindowManager.openWindow("/com/groupeisi/minisystemebancaire/client/credits.fxml", "Demandes de Crédit");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture des crédits", e);
        }
    }

    @FXML
    private void ouvrirSupport() {
        System.out.println("Ouverture du support client");
        try {
            WindowManager.openWindow("/com/groupeisi/minisystemebancaire/client/support.fxml", "Support Client");
        } catch (Exception e) {
            handleError("Erreur lors de l'ouverture du support", e);
        }
    }

    @FXML
    private void seDeconnecter() {
        System.out.println("Déconnexion du client");

        boolean confirm = WindowManager.showConfirmation(
                "Déconnexion",
                "Confirmer la déconnexion",
                "Êtes-vous sûr de vouloir vous déconnecter ?"
        );

        if (confirm) {
            // Nettoyer les données de session
            ApiConfig.logout();

            // Rediriger vers la page de connexion
            try {
                WindowManager.closeWindow();
                WindowManager.openWindow("/com/groupeisi/minisystemebancaire/connexion.fxml", "Connexion");
            } catch (Exception e) {
                handleError("Erreur lors de la déconnexion", e);
            }
        }
    }

    // Méthodes utilitaires

    private void handleError(String message, Throwable throwable) {
        System.err.println(message + ": " + throwable.getMessage());
        throwable.printStackTrace();

        Platform.runLater(() -> {
            String errorMessage = message;
            if (throwable != null && throwable.getMessage() != null) {
                errorMessage += ": " + throwable.getMessage();
            }
            WindowManager.showError("Erreur", "Erreur de l'application", errorMessage);
        });
    }

    private void redirectToLogin() {
        try {
            WindowManager.closeWindow();
            WindowManager.openWindow("/com/groupeisi/minisystemebancaire/connexion.fxml", "Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}