package gm.rahmanproperties.optibank.controllers.client;

import com.jfoenix.controls.JFXButton;
import gm.rahmanproperties.optibank.config.ApiConfig;
import gm.rahmanproperties.optibank.dtos.*;
import gm.rahmanproperties.optibank.utils.CurrencyFormatter;
import gm.rahmanproperties.optibank.utils.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardClientController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private Label soldeLabel;
    @FXML private Label nombreTransactionsLabel;

    @FXML private TableView<CompteDTo> comptesTable;
    @FXML private TableColumn<CompteDTo, String> accountNumberCol;
    @FXML private TableColumn<CompteDTo, String> accountTypeCol;
    @FXML private TableColumn<CompteDTo, Double> accountBalanceCol;

    @FXML private TableView<TransactionDTo> dernieresTransactionsTable;
    @FXML private PieChart depensesChart;

    @FXML private JFXButton virementsBtn;
    @FXML private JFXButton cartesBtn;
    @FXML private JFXButton creditsBtn;
    @FXML private JFXButton supportBtn;

    private final RestTemplate restTemplate = new RestTemplate();
    private ClientDTo clientConnecte;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTables();
        chargerClientConnecte();
    }

    private void configurerTables() {
        // Configuration de la table des comptes
        accountNumberCol.setCellValueFactory(new PropertyValueFactory<>("numeroCompte"));
        accountTypeCol.setCellValueFactory(new PropertyValueFactory<>("typeCompte"));
        accountBalanceCol.setCellValueFactory(new PropertyValueFactory<>("solde"));
        accountBalanceCol.setCellFactory(column -> new TableCell<CompteDTo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(CurrencyFormatter.format(item));
                }
            }
        });

        // Configuration de la table des transactions
        TableColumn<TransactionDTo, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateTransaction"));

        TableColumn<TransactionDTo, Double> montantCol = new TableColumn<>("Montant");
        montantCol.setCellValueFactory(new PropertyValueFactory<>("montant"));
        montantCol.setCellFactory(column -> new TableCell<TransactionDTo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(CurrencyFormatter.format(item));
                }
            }
        });

        TableColumn<TransactionDTo, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        dernieresTransactionsTable.getColumns().setAll(dateCol, montantCol, descriptionCol);
    }

    private void chargerClientConnecte() {
        try {
            clientConnecte = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/api/clients/"+ clientConnecte.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    ClientDTo.class
            ).getBody();

            if (clientConnecte != null) {
                welcomeLabel.setText("Bienvenue, " + clientConnecte.getPrenom() + " " + clientConnecte.getNom());
                chargerComptes();
                chargerTransactions();
            }
        } catch (Exception e) {
            WindowManager.showError("Erreur",
                    "Impossible de charger les données du client",
                    e.getMessage());
        }
    }

    private void chargerComptes() {
        try {
            CompteDTo[] comptes = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/api/comptes/client/" + clientConnecte.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    CompteDTo[].class
            ).getBody();

            if (comptes != null) {
                comptesTable.setItems(FXCollections.observableArrayList(comptes));
                mettreAJourSoldeTotal(comptes);
            }
        } catch (Exception e) {
            WindowManager.showError("Erreur",
                    "Impossible de charger les comptes",
                    e.getMessage());
        }
    }

    private void chargerTransactions() {
        try {
            TransactionDTo[] transactions = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/api/transactions/client/" + clientConnecte.getId() + "?limit=5",
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    TransactionDTo[].class
            ).getBody();

            if (transactions != null) {
                dernieresTransactionsTable.setItems(FXCollections.observableArrayList(transactions));
                nombreTransactionsLabel.setText(String.valueOf(transactions.length));
                mettreAJourGraphiqueDepenses(transactions);
            }
        } catch (Exception e) {
            WindowManager.showError("Erreur",
                    "Impossible de charger les transactions",
                    e.getMessage());
        }
    }

    private void mettreAJourSoldeTotal(CompteDTo[] comptes) {
        double soldeTotal = 0;
        for (CompteDTo compte : comptes) {
            soldeTotal += Integer.parseInt(String.valueOf(compte.getSolde()));
        }
        soldeLabel.setText(CurrencyFormatter.format(soldeTotal));
    }

    private void mettreAJourGraphiqueDepenses(TransactionDTo[] transactions) {
        Map<String, Double> categoriesDepenses = analyserCategoriesDepenses(transactions);
        depensesChart.getData().clear();

        categoriesDepenses.forEach((categorie, montant) -> {
            if (montant > 0) {
                depensesChart.getData().add(new PieChart.Data(categorie, montant));
            }
        });
    }

    private Map<String, Double> analyserCategoriesDepenses(TransactionDTo[] transactions) {
        Map<String, Double> result = new HashMap<>();
        result.put("Achats", 0.0);
        result.put("Services", 0.0);
        result.put("Virements", 0.0);
        result.put("Autres", 0.0);

        for (TransactionDTo transaction : transactions) {
            if (transaction.getMontant().equals(BigDecimal.ZERO)) {
                double montant = Math.abs(Integer.parseInt(String.valueOf(transaction.getMontant())));
                String categorie = determinerCategorie(transaction);
                result.put(categorie, result.getOrDefault(categorie, 0.0) + montant);
            }
        }

        return result;
    }

    private String determinerCategorie(TransactionDTo transaction) {
        if (transaction.getDescription().toLowerCase().contains("achat")) {
            return "Achats";
        } else if (transaction.getDescription().toLowerCase().contains("service")) {
            return "Services";
        } else if (transaction.getDescription().toLowerCase().contains("virement")) {
            return "Virements";
        }
        return "Autres";
    }

    @FXML
    private void ouvrirVirements() {
        WindowManager.openWindow("/fxml/client/client_transaction.fxml", "Effectuer un virement");
    }

    @FXML
    private void ouvrirCartesBancaires() {
        WindowManager.openWindow("/fxml/client/carte_bancaire.fxml", "Mes cartes bancaires");
    }

    @FXML
    private void ouvrirCredits() {
        WindowManager.openModalWindow("/fxml/client/credit_carte.fxml", "Mes crédits");
    }

    @FXML
    private void ouvrirSupport() {
        WindowManager.openWindow("/fxml/client/support.fxml", "Support client");
    }

    @FXML
    private void actualiserDonnees() {
        chargerComptes();
        chargerTransactions();
    }
}
