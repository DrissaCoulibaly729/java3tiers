package gm.rahmanproperties.optibank.controllers.client;

import gm.rahmanproperties.optibank.config.ApiConfig;
import gm.rahmanproperties.optibank.dtos.TransactionDTo;
import gm.rahmanproperties.optibank.dtos.CompteDTo;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ClientTransactionController implements Initializable {
    @FXML private TableView<TransactionDTo> transactionTable;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = ApiConfig.getApiUrl() + "/api/transactions";
    private CompteDTo currentAccount;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableView();
    }

    public void setCompte(CompteDTo compte) {
        this.currentAccount = compte;
        chargerTransactions();
    }

    private void setupTableView() {
        // Configuration des colonnes de la table...
    }

    private void chargerTransactions() {
//        if (currentAccount == null) return;
//
//        try {
//            TransactionDTo[] transactions = restTemplate.getForObject(
//                API_URL + "/compte/" + currentAccount.getId(),
//                TransactionDTo[].class
//            );
//            if (transactions != null) {
//                transactionTable.getItems().setAll(transactions);
//            }
//        } catch (Exception e) {
//            // Gérer l'erreur et afficher un message à l'utilisateur
//        }
    }

    @FXML
    private void effectuerVirement() {
        // Récupérer les valeurs du formulaire
        Long compteDestination = null; // À récupérer de l'interface
        BigDecimal montant = null; // À récupérer de l'interface

        var request = Map.of(
            "sourceId", currentAccount.getType(),
            "destinationId", compteDestination,
            "montant", montant
        );

        try {
            restTemplate.postForObject(
                ApiConfig.getApiUrl() + "/api/comptes/virement",
                request,
                Void.class
            );
            // Actualiser l'affichage et montrer un message de succès
            chargerTransactions();
        } catch (Exception e) {
            // Gérer l'erreur et afficher un message à l'utilisateur
        }
    }

    @FXML
    private void effectuerDepot() {
        BigDecimal montant = null; // À récupérer de l'interface

        var request = Map.of("montant", montant);

        try {
            restTemplate.postForObject(
                ApiConfig.getApiUrl() + "/api/comptes/" + currentAccount.getType() + "/depot",
                request,
                CompteDTo.class
            );
            // Actualiser l'affichage et montrer un message de succès
            chargerTransactions();
        } catch (Exception e) {
            // Gérer l'erreur et afficher un message à l'utilisateur
        }
    }

    @FXML
    private void effectuerRetrait() {
        BigDecimal montant = null; // À récupérer de l'interface

        var request = Map.of("montant", montant);

        try {
            restTemplate.postForObject(
                ApiConfig.getApiUrl() + "/api/comptes/" + currentAccount.getClientId() + "/retrait",
                request,
                CompteDTo.class
            );
            // Actualiser l'affichage et montrer un message de succès
            chargerTransactions();
        } catch (Exception e) {
            // Gérer l'erreur et afficher un message à l'utilisateur
        }
    }
}
