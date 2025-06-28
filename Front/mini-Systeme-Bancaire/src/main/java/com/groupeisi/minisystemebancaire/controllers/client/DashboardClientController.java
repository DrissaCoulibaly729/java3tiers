package com.groupeisi.minisystemebancaire.controllers.client;

import com.jfoenix.controls.JFXButton;
import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.*;
import com.groupeisi.minisystemebancaire.services.*;
import com.groupeisi.minisystemebancaire.utils.CurrencyFormatter;
import com.groupeisi.minisystemebancaire.utils.WindowManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardClientController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private Label soldeLabel;
    @FXML private Label nombreTransactionsLabel;

    @FXML private TableView<CompteDTo> comptesTable;
    @FXML private TableColumn<CompteDTo, String> accountNumberCol;
    @FXML private TableColumn<CompteDTo, String> accountTypeCol;
    @FXML private TableColumn<CompteDTo, BigDecimal> accountBalanceCol;

    @FXML private TableView<TransactionDTo> dernieresTransactionsTable;
    @FXML private TableColumn<TransactionDTo, String> transactionTypeCol;
    @FXML private TableColumn<TransactionDTo, BigDecimal> transactionMontantCol;
    @FXML private TableColumn<TransactionDTo, String> transactionDateCol;
    @FXML private TableColumn<TransactionDTo, String> transactionStatutCol;

    @FXML private PieChart depensesChart;

    @FXML private JFXButton virementsBtn;
    @FXML private JFXButton cartesBtn;
    @FXML private JFXButton creditsBtn;
    @FXML private JFXButton supportBtn;

    // Services
    private final ClientService clientService = new ClientService();
    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();

    private ClientDTo clientConnecte;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTables();
        configurerBoutons();
        chargerDonneesClient();
    }

    private void configurerTables() {
        // Configuration de la table des comptes
        accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("numeroCompte"));
        accountTypeCol.setCellValueFactory(new PropertyValueFactory<>("typeCompte"));
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

        // Configuration de la table des transactions
        transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
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

        transactionDateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        transactionDateCol.setCellFactory(column -> new TableCell<TransactionDTo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    TransactionDTo transaction = getTableRow().getItem();
                    if (transaction != null && transaction.getCreatedAt() != null) {
                        setText(transaction.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    }
                }
            }
        });

        transactionStatutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
    }

    private void configurerBoutons() {
        virementsBtn.setOnAction(e -> ouvrirFenetre("/fxml/client/virements.fxml", "Virements"));
        cartesBtn.setOnAction(e -> ouvrirFenetre("/fxml/client/cartes.fxml", "Gestion des Cartes"));
        creditsBtn.setOnAction(e -> ouvrirFenetre("/fxml/client/credits.fxml", "Crédits"));
        supportBtn.setOnAction(e -> ouvrirFenetre("/fxml/client/support.fxml", "Support Client"));
    }

    private void chargerDonneesClient() {
        Long clientId = ApiConfig.getCurrentUserId();
        if (clientId == null) {
            Platform.runLater(() -> {
                WindowManager.showFxPopupError("Session expirée\nVeuillez vous reconnecter.");
                // Rediriger vers la connexion
                try {
                    WindowManager.closeWindow();
                    WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        // Charger les informations du client
        clientService.getClientById(clientId)
                .thenAccept(client -> {
                    this.clientConnecte = client;
                    Platform.runLater(() -> {
                        welcomeLabel.setText("Bienvenue, " + client.getPrenom() + " " + client.getNom());
                    });
                })
                .exceptionally(throwable -> {
                    handleError("Erreur lors du chargement du profil client", throwable);
                    return null;
                });

        // Charger les comptes
        compteService.getComptesByClient(clientId)
                .thenAccept(comptes -> {
                    Platform.runLater(() -> {
                        comptesTable.setItems(FXCollections.observableArrayList(comptes));

                        // Calculer le solde total
                        BigDecimal soldeTotal = comptes.stream()
                                .map(CompteDTo::getSolde)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        soldeLabel.setText(CurrencyFormatter.format(soldeTotal.doubleValue()));
                    });
                })
                .exceptionally(throwable -> {
                    handleError("Erreur lors du chargement des comptes", throwable);
                    return null;
                });

        // Charger les dernières transactions
        transactionService.getTransactionsByClient(clientId)
                .thenAccept(transactions -> {
                    Platform.runLater(() -> {
                        // Prendre les 10 dernières transactions
                        List<TransactionDTo> dernieresTransactions = transactions.stream()
                                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                                .limit(10)
                                .toList();

                        dernieresTransactionsTable.setItems(FXCollections.observableArrayList(dernieresTransactions));
                        nombreTransactionsLabel.setText(String.valueOf(transactions.size()));

                        // Mettre à jour le graphique des dépenses
                        mettreAJourGraphiqueDepenses(transactions);
                    });
                })
                .exceptionally(throwable -> {
                    handleError("Erreur lors du chargement des transactions", throwable);
                    return null;
                });
    }

    private void mettreAJourGraphiqueDepenses(List<TransactionDTo> transactions) {
        Map<String, BigDecimal> depensesParType = new HashMap<>();

        transactions.stream()
                .filter(t -> "Retrait".equals(t.getType()) || "Virement".equals(t.getType()))
                .forEach(transaction -> {
                    depensesParType.merge(transaction.getType(),
                            transaction.getMontant(),
                            BigDecimal::add);
                });

        depensesChart.getData().clear();
        depensesParType.forEach((type, montant) -> {
            PieChart.Data slice = new PieChart.Data(type, montant.doubleValue());
            depensesChart.getData().add(slice);
        });
    }

    private void ouvrirFenetre(String fxmlPath, String titre) {
        try {
            WindowManager.openModalWindow(fxmlPath, titre);
        } catch (Exception e) {
            handleError("Erreur d'ouverture de fenêtre", e);
        }
    }

    private void handleError(String message, Throwable throwable) {
        Platform.runLater(() -> {
            String errorMessage = message;
            if (throwable != null) {
                errorMessage += "\n" + throwable.getMessage();
            }
            WindowManager.showFxPopupError(errorMessage);
            System.err.println("Erreur: " + message);
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });
    }

    @FXML
    public void handleLogout() {
        ApiConfig.clearSession();
        try {
            WindowManager.closeWindow();
            WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
        } catch (Exception e) {
            handleError("Erreur de déconnexion", e);
        }
    }
}