package com.groupeisi.minisystemebancaire.controllers.client;

import com.groupeisi.minisystemebancaire.config.ApiConfig;
import com.groupeisi.minisystemebancaire.dtos.TransactionDTo;
import com.groupeisi.minisystemebancaire.dtos.CompteDTo;
import com.groupeisi.minisystemebancaire.services.HttpService;
import com.groupeisi.minisystemebancaire.services.TransactionService;
import com.groupeisi.minisystemebancaire.services.CompteService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ClientTransactionController implements Initializable {

    // Table des transactions
    @FXML private TableView<TransactionDTo> transactionTable;
    @FXML private TableColumn<TransactionDTo, String> typeCol;
    @FXML private TableColumn<TransactionDTo, BigDecimal> montantCol;
    @FXML private TableColumn<TransactionDTo, String> descriptionCol;
    @FXML private TableColumn<TransactionDTo, String> statutCol;
    @FXML private TableColumn<TransactionDTo, LocalDateTime> dateCol;

    // Filtres
    @FXML private ComboBox<CompteDTo> compteComboBox;
    @FXML private ComboBox<String> typeTransactionComboBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Button filtrerButton;
    @FXML private Button reinitialiserButton;

    // Informations
    @FXML private Label totalTransactionsLabel;
    @FXML private Label soldeCompteLabel;
    @FXML private Label derniereMajLabel;

    private Long clientId;
    private CompteDTo compteSelectionne;
    private List<CompteDTo> comptesClient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier l'authentification
        clientId = ApiConfig.getCurrentUserId();
        if (clientId == null) {
            redirectToLogin();
            return;
        }

        configurerTable();
        configurerFiltres();
        configurerBoutons();
        chargerDonnees();
    }

    private void configurerTable() {
        // Configuration des colonnes
        if (typeCol != null) {
            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            typeCol.setCellFactory(column -> new TableCell<TransactionDTo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(formatTypeTransaction(item));
                        // Coloration selon le type
                        switch (item.toLowerCase()) {
                            case "depot":
                                setStyle("-fx-text-fill: green;");
                                break;
                            case "retrait":
                                setStyle("-fx-text-fill: red;");
                                break;
                            case "virement":
                                setStyle("-fx-text-fill: blue;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });
        }

        if (montantCol != null) {
            montantCol.setCellValueFactory(new PropertyValueFactory<>("montant"));
            montantCol.setCellFactory(column -> new TableCell<TransactionDTo, BigDecimal>() {
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

        if (descriptionCol != null) {
            descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        }

        if (statutCol != null) {
            statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
            statutCol.setCellFactory(column -> new TableCell<TransactionDTo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(formatStatut(item));
                        switch (item.toLowerCase()) {
                            case "validee":
                                setStyle("-fx-text-fill: green;");
                                break;
                            case "en_attente":
                                setStyle("-fx-text-fill: orange;");
                                break;
                            case "annulee":
                                setStyle("-fx-text-fill: red;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });
        }

        if (dateCol != null) {
            dateCol.setCellValueFactory(new PropertyValueFactory<>("dateTransaction"));
            dateCol.setCellFactory(column -> new TableCell<TransactionDTo, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    }
                }
            });
        }
    }

    private void configurerFiltres() {
        // Types de transactions
        if (typeTransactionComboBox != null) {
            typeTransactionComboBox.setItems(FXCollections.observableArrayList(
                    "Tous", "depot", "retrait", "virement"
            ));
            typeTransactionComboBox.setValue("Tous");
        }
    }

    private void configurerBoutons() {
        if (filtrerButton != null) {
            filtrerButton.setOnAction(e -> appliquerFiltres());
        }
        if (reinitialiserButton != null) {
            reinitialiserButton.setOnAction(e -> reinitialiserFiltres());
        }
    }

    private void chargerDonnees() {
        chargerComptes();
    }

    private void chargerComptes() {
        CompteService.getComptesByClient(clientId)
                .thenAccept(comptes -> {
                    Platform.runLater(() -> {
                        this.comptesClient = comptes;
                        if (compteComboBox != null && comptes != null) {
                            compteComboBox.setItems(FXCollections.observableArrayList(comptes));

                            // Converter pour afficher le numéro de compte
                            compteComboBox.setConverter(new javafx.util.StringConverter<CompteDTo>() {
                                @Override
                                public String toString(CompteDTo compte) {
                                    return compte != null ?
                                            compte.getNumeroCompte() + " (" + compte.getType() + ")" : "";
                                }

                                @Override
                                public CompteDTo fromString(String string) {
                                    return null;
                                }
                            });

                            // Listener pour changement de compte
                            compteComboBox.getSelectionModel().selectedItemProperty().addListener(
                                    (obs, oldCompte, newCompte) -> {
                                        compteSelectionne = newCompte;
                                        if (newCompte != null) {
                                            chargerTransactions(newCompte);
                                            mettreAJourInfosCompte(newCompte);
                                        }
                                    }
                            );

                            // Sélectionner le premier compte par défaut
                            if (!comptes.isEmpty()) {
                                compteComboBox.getSelectionModel().selectFirst();
                            }
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de charger les comptes",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    private void chargerTransactions(CompteDTo compte) {
        if (compte == null) return;

        TransactionService.getTransactionsByCompte(compte.getId())
                .thenAccept(transactions -> {
                    Platform.runLater(() -> {
                        if (transactionTable != null && transactions != null) {
                            transactionTable.setItems(FXCollections.observableArrayList(transactions));

                            if (totalTransactionsLabel != null) {
                                totalTransactionsLabel.setText(String.valueOf(transactions.size()));
                            }

                            if (derniereMajLabel != null) {
                                derniereMajLabel.setText(LocalDateTime.now()
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                            }
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible de charger les transactions",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    private void mettreAJourInfosCompte(CompteDTo compte) {
        if (soldeCompteLabel != null && compte != null) {
            soldeCompteLabel.setText(CurrencyFormatter.format(compte.getSolde().doubleValue()));
        }
    }

    @FXML
    private void appliquerFiltres() {
        if (compteSelectionne == null) {
            WindowManager.showWarning("Attention",
                    "Aucun compte sélectionné",
                    "Veuillez sélectionner un compte pour filtrer les transactions.");
            return;
        }

        // Construire les paramètres de filtre
        StringBuilder endpoint = new StringBuilder("/api/transactions/historique/" + clientId + "?");

        if (dateDebutPicker != null && dateDebutPicker.getValue() != null) {
            endpoint.append("date_debut=").append(dateDebutPicker.getValue()).append("&");
        }

        if (dateFinPicker != null && dateFinPicker.getValue() != null) {
            endpoint.append("date_fin=").append(dateFinPicker.getValue()).append("&");
        }

        if (typeTransactionComboBox != null &&
                typeTransactionComboBox.getValue() != null &&
                !"Tous".equals(typeTransactionComboBox.getValue())) {
            endpoint.append("type=").append(typeTransactionComboBox.getValue()).append("&");
        }

        endpoint.append("compte_id=").append(compteSelectionne.getId());

        // Charger les transactions filtrées
        HttpService.getListAsync(endpoint.toString(), TransactionDTo.class)
                .thenAccept(transactions -> {
                    Platform.runLater(() -> {
                        if (transactionTable != null && transactions != null) {
                            transactionTable.setItems(FXCollections.observableArrayList(transactions));

                            if (totalTransactionsLabel != null) {
                                totalTransactionsLabel.setText(String.valueOf(transactions.size()));
                            }
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        WindowManager.showError("Erreur",
                                "Impossible d'appliquer les filtres",
                                throwable.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    private void reinitialiserFiltres() {
        if (dateDebutPicker != null) dateDebutPicker.setValue(null);
        if (dateFinPicker != null) dateFinPicker.setValue(null);
        if (typeTransactionComboBox != null) typeTransactionComboBox.setValue("Tous");

        // Recharger toutes les transactions du compte sélectionné
        if (compteSelectionne != null) {
            chargerTransactions(compteSelectionne);
        }
    }

    @FXML
    private void exporterTransactions() {
        // TODO: Implémenter l'export des transactions
        WindowManager.showWarning("Fonctionnalité",
                "Export en cours de développement",
                "Cette fonctionnalité sera disponible prochainement.");
    }

    private String formatTypeTransaction(String type) {
        switch (type.toLowerCase()) {
            case "depot": return "Dépôt";
            case "retrait": return "Retrait";
            case "virement": return "Virement";
            default: return type;
        }
    }

    private String formatStatut(String statut) {
        switch (statut.toLowerCase()) {
            case "validee": return "Validée";
            case "en_attente": return "En attente";
            case "annulee": return "Annulée";
            default: return statut;
        }
    }

    private void redirectToLogin() {
        try {
            WindowManager.closeWindow();
            WindowManager.openWindow("/fxml/connexion.fxml", "Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}