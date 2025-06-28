package gm.rahmanproperties.optibank.controllers.admin;

import gm.rahmanproperties.optibank.config.ApiConfig;
import gm.rahmanproperties.optibank.dtos.*;
import gm.rahmanproperties.optibank.utils.ValidationUtils;
import gm.rahmanproperties.optibank.utils.WindowManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardAdminController implements Initializable {
    @FXML
    private TableView<TransactionDTo> transactionsTable;
    @FXML
    private TableView<CreditDTo> creditsTable;
    @FXML
    private TableView<TicketSupportDTo> ticketsTable;
    @FXML
    private ComboBox<?> listClient;
    @FXML
    private Label messageField;
    @FXML
    private Button btnButton;
    @FXML
    private LineChart<String, Number> statisticsChart;
    @FXML
    private Text totalClientsLabel;
    @FXML
    private Text totalTransactionsLabel;
    @FXML
    private Text totalCreditsLabel;
    @FXML
    private TextField adresseField;
    @FXML
    private ComboBox<CompteDTo.TypeCompte> cbbTypeCompte;
    @FXML
    private TextField emailField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField soldeField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField usernameField;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerDonnees();
        cbbTypeCompte.getItems().addAll(List.of(CompteDTo.TypeCompte.values()));
        cbbTypeCompte.getSelectionModel().selectFirst();
//        System.out.println(getTotalClients().stream().count());
        totalClientsLabel.setText(String.valueOf(getTotalClients()));
        totalTransactionsLabel.setText(String.valueOf(getTotalTransactions()));
        totalCreditsLabel.setText(String.valueOf(getTotalCredits()));

        NumberAxis yAxis = (NumberAxis) statisticsChart.getYAxis();
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return formatXOF(BigDecimal.valueOf(object.doubleValue()));
            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        });

        // Appeler le chargement des données
        chargerDonnees();
    }

    private void chargerDonnees() {
        chargerTransactionsSuspectes();
        chargerCreditsEnCours();
        chargerTicketsNonResolus();
        setupTransactionsTable();
        setupTicketsTable();
        setupCreditsTable();
        chargerStatistiques();
    }

    private void chargerTransactionsSuspectes() {
        try {
            TransactionDTo[] transactions = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/transactions/suspectes",
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    TransactionDTo[].class
            ).getBody();

            if (transactions != null) {
                transactionsTable.getItems().setAll(transactions);
            }
        } catch (Exception e) {
            WindowManager.showFxPopupError("Impossible de charger les transactions suspectes" +
                    e.getMessage());
        }
    }

    private void chargerCreditsEnCours() {
        try {
            CreditDTo[] credits = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/api/credits/en-cours",
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    CreditDTo[].class
            ).getBody();

            if (credits != null) {
                creditsTable.getItems().setAll(credits);
            }
        } catch (Exception e) {
            WindowManager.showFxPopupError("Impossible de charger les crédits en cours" +
                    e.getMessage());
        }
    }

    private void chargerTicketsNonResolus() {
        try {
            TicketSupportDTo[] tickets = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/api/support/tickets/statut/EN_COURS",
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    TicketSupportDTo[].class
            ).getBody();

            System.out.println("Tickets non résolus chargés : " + (tickets != null ? tickets.length : 0));

            if (tickets != null) {
                ticketsTable.getItems().setAll(tickets);
            }
        } catch (Exception e) {
            WindowManager.showFxPopupError("Impossible de charger les tickets non résolus" +
                    e.getMessage());
        }
    }

    private void setupTransactionsTable() {
        TableColumn<TransactionDTo, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateTransaction().toString()));

        TableColumn<TransactionDTo, String> montantCol = new TableColumn<>("Montant");
        montantCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMontant().toString() + " XOF"));

        TableColumn<TransactionDTo, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType().toString()));

        transactionsTable.getColumns().setAll(dateCol, montantCol, typeCol);
    }

    private void setupCreditsTable() {
        TableColumn<CreditDTo, String> referenceCol = new TableColumn<>("Référence");
        referenceCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getReference()));

        TableColumn<CreditDTo, String> montantCol = new TableColumn<>("Montant");
        montantCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMontant().toString() + " XOF"));

        TableColumn<CreditDTo, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut().toString()));

        creditsTable.getColumns().setAll(referenceCol, montantCol, statutCol);
    }

    private void setupTicketsTable() {
        TableColumn<TicketSupportDTo, String> sujetCol = new TableColumn<>("Sujet");
        sujetCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSujet()));

        TableColumn<TicketSupportDTo, String> prioriteCol = new TableColumn<>("Priorité");
        prioriteCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPriorite().toString()));

        TableColumn<TicketSupportDTo, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatut().toString()));

        ticketsTable.getColumns().setAll(sujetCol, prioriteCol, statutCol);
    }

    private int getTotalClients() {
        try {
            ResponseEntity<ClientDTo[]> response = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/api/clients",
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    ClientDTo[].class
            );
            return response.getBody() != null ? response.getBody().length : 0;
        } catch (Exception e) {
            WindowManager.showFxPopupError("Erreur clients: " + e.getMessage());
            return 0;
        }
    }

    private int getTotalTransactions() {
        try {
            ResponseEntity<TransactionDTo[]> response = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/transactions/suspectes",
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    TransactionDTo[].class
            );
            return response.getBody() != null ? response.getBody().length : 0;
        } catch (Exception e) {
            WindowManager.showFxPopupError("Erreur transactions: " + e.getMessage());
            return 0;
        }
    }

    private int getTotalCredits() {
        try {
            ResponseEntity<CreditDTo[]> response = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/credits/retard",
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    CreditDTo[].class
            );
            return response.getBody() != null ? response.getBody().length : 0;
        } catch (Exception e) {
            WindowManager.showFxPopupError("Erreur crédits: " + e.getMessage());
            return 0;
        }
    }

    private void chargerStatistiques() {
        try {
            // Récupérer les transactions des 6 derniers mois pour les statistiques
            ResponseEntity<TransactionDTo[]> response = restTemplate.exchange(
                        ApiConfig.getApiUrl() + "/transactions/derniers-mois?mois=6",
                    HttpMethod.GET,
                    new HttpEntity<>(null, ApiConfig.createHeaders()),
                    TransactionDTo[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Grouper les transactions par mois et calculer le total
                Map<String, BigDecimal> transactionsParMois = Arrays.stream(response.getBody())
                        .collect(Collectors.groupingBy(
                                t -> t.getDateTransaction().getMonth().toString(),
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        TransactionDTo::getMontant,
                                        BigDecimal::add
                                )
                        ));

                // Convertir en format pour le graphique
                List<Map<String, Object>> donnees = transactionsParMois.entrySet().stream()
                        .map(entry -> Map.<String, Object>of(
                                "mois", entry.getKey(),
                                "total", entry.getValue()
                        ))
                        .collect(Collectors.toList());

                mettreAJourGraphique(donnees);
            }
        } catch (Exception e) {
            WindowManager.showFxPopupError("Erreur lors du chargement des statistiques: " + e.getMessage());

            // Données de démo en cas d'erreur
            List<Map<String, Object>> donneesDemo = List.of(
                    Map.of("mois", "Janvier", "total", new BigDecimal("1500000")),
                    Map.of("mois", "Février", "total", new BigDecimal("2300000")),
                    Map.of("mois", "Mars", "total", new BigDecimal("1800000")),
                    Map.of("mois", "Avril", "total", new BigDecimal("3100000")),
                    Map.of("mois", "Mai", "total", new BigDecimal("2900000")),
                    Map.of("mois", "Juin", "total", new BigDecimal("3500000"))
            );
            mettreAJourGraphique(donneesDemo);
        }
    }

    private void mettreAJourGraphique(List<Map<String, Object>> donnees) {
        statisticsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Évolution des transactions");

        // Trier les données par mois (vous pourriez avoir besoin d'un ordre spécifique)
        donnees.sort(Comparator.comparing(d -> {
            String mois = (String) d.get("mois");
            return List.of("Janvier", "Février", "Mars", "Avril", "Mai", "Juin").indexOf(mois);
        }));

        for (Map<String, Object> point : donnees) {
            String mois = (String) point.get("mois");
            BigDecimal total = (BigDecimal) point.get("total");
            series.getData().add(new XYChart.Data<>(mois, total));
        }

        statisticsChart.getData().add(series);

        // Personnalisation du graphique
        statisticsChart.setTitle("Évolution des transactions (XOF)");
        statisticsChart.setLegendVisible(false);
        statisticsChart.setCreateSymbols(true);
        statisticsChart.setAnimated(false);

        // Style des séries
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-stroke: #078d65; -fx-stroke-width: 2px;");
        }
    }

    private String formatXOF(BigDecimal montant) {
        DecimalFormat format = new DecimalFormat("#,### XOF");
        return format.format(montant);
    }

    @FXML
    public void handleSave(javafx.event.ActionEvent actionEvent) {
        if (!ValidationUtils.isValidEmail(emailField.getText())) {
            WindowManager.showFxPopupError("Adresse email invalide");
            return;
        }

        if (!ValidationUtils.isValidPassword(passwordField.getText())) {
            WindowManager.showFxPopupError("Mot de passe doit contenir au moins 8 caractères et inclure des majuscules, minuscules, chiffres et caractères spéciaux");
            return;
        }

        if (!ValidationUtils.isValidPhone(telephoneField.getText())) {
            WindowManager.showFxPopupError("Numéro de téléphone invalide");
            return;
        }

        if (!ValidationUtils.isValidAmount(soldeField.getText())) {
            WindowManager.showFxPopupError("Solde doit être un nombre positif");
            return;
        }

        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                adresseField.getText().isEmpty() || usernameField.getText().isEmpty()) {
            WindowManager.showFxPopupError("Tous les champs sont obligatoires");
            return;
        }

        ClientDTo clientDTo = new ClientDTo();
        clientDTo.setNom(nomField.getText());
        clientDTo.setPrenom(prenomField.getText());
        clientDTo.setEmail(emailField.getText());
        clientDTo.setTelephone(telephoneField.getText());
        clientDTo.setRoles(Collections.singleton(ClientDTo.Role.ROLE_CLIENT));
        clientDTo.setPassword(passwordField.getText());
        clientDTo.setAdresse(adresseField.getText());
        clientDTo.setUsername(usernameField.getText());
        try {
            ResponseEntity<ClientDTo> exchange = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/clients",
                    HttpMethod.POST,
                    new HttpEntity<>(clientDTo, ApiConfig.createHeaders()),
                    ClientDTo.class
            );

            System.out.println("Response Status: " + exchange.getStatusCode());
            System.out.println("Response Body: " + exchange.getBody());

            if (exchange.getStatusCode().is2xxSuccessful()) {
                clientDTo = exchange.getBody();
                System.out.println(clientDTo);
                if (clientDTo == null) {
                    throw new Exception("Client non créé, réponse vide");
                }

                CompteDTo compteDTo = new CompteDTo();
                compteDTo.setType(CompteDTo.TypeCompte.valueOf(String.valueOf((cbbTypeCompte.getValue()))));
                compteDTo.setSolde(new BigDecimal(soldeField.getText()));
                compteDTo.setClientId(clientDTo.getId());

                ResponseEntity<CompteDTo> compteResponse = restTemplate.exchange(
                        ApiConfig.getApiUrl() + "/api/comptes?clientId="+clientDTo.getId(),
                        HttpMethod.POST,
                        new HttpEntity<>(compteDTo, ApiConfig.createHeaders()),
                        CompteDTo.class
                );

                if (!compteResponse.getStatusCode().is2xxSuccessful()) {
                    throw new Exception("Erreur lors de la création du compte : " + compteResponse.getStatusCode());
                }

                compteDTo = compteResponse.getBody();
                if (compteDTo == null) {
                    throw new Exception("Compte non créé, réponse vide");
                }

            } else {
                throw new Exception("Erreur lors de la création du client : " + exchange.getStatusCode());
            }

        } catch (Exception e) {
            WindowManager.showFxPopupError("Erreur lors de la création du client : " + e.getMessage());
            return;
        }

        WindowManager.showFxPopupSuccess("Client créé avec succès");
        chargerDonnees();
    }

    @FXML
    public void handleAction(ActionEvent actionEvent) {

    }
}
