package gm.rahmanproperties.optibank.controllers.client;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import gm.rahmanproperties.optibank.utils.SimulationPlacement;
import gm.rahmanproperties.optibank.utils.WindowManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class SimulationPlacementController implements Initializable {

    @FXML private JFXTextField montantInitialField;
    @FXML private JFXTextField dureeAnneesField;
    @FXML private JFXTextField tauxInteretField;
    @FXML private JFXCheckBox interetsComposesCheck;
    @FXML private JFXButton simulerButton;
    @FXML private VBox resultatContainer;
    @FXML private Label montantFinalLabel;
    @FXML private Label interetsGeneresLabel;
    @FXML private LineChart<Number, Number> evolutionChart;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupValidation();
        setupChart();
        simulerButton.setOnAction(e -> simuler());
    }

    private void setupValidation() {
        // Configuration des validateurs pour les champs numériques
        montantInitialField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                montantInitialField.setText(old);
            }
        });

        dureeAnneesField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d*")) {
                dureeAnneesField.setText(old);
            }
        });

        tauxInteretField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                tauxInteretField.setText(old);
            }
        });
    }

    private void setupChart() {
        evolutionChart.setTitle("Évolution du placement");
        evolutionChart.getXAxis().setLabel("Années");
        evolutionChart.getYAxis().setLabel("Montant (€)");
    }

    @FXML
    private void simuler() {
        try {
            BigDecimal montantInitial = new BigDecimal(montantInitialField.getText());
            int dureeAnnees = Integer.parseInt(dureeAnneesField.getText());
            BigDecimal tauxInteret = new BigDecimal(tauxInteretField.getText());
            boolean interetsComposes = interetsComposesCheck.isSelected();

            SimulationPlacement.ResultatSimulation resultat = SimulationPlacement.simulerPlacement(
                montantInitial, dureeAnnees, tauxInteret, interetsComposes
            );

            // Affichage des résultats
            montantFinalLabel.setText("Montant final : " + currencyFormatter.format(resultat.getMontantFinal()));
            interetsGeneresLabel.setText("Intérêts générés : " + currencyFormatter.format(resultat.getInteretsGeneres()));

            // Mise à jour du graphique
            updateChart(resultat);
            
            resultatContainer.setVisible(true);

        } catch (NumberFormatException e) {
            WindowManager.showError("Erreur", "Données invalides", 
                "Veuillez vérifier que tous les champs sont correctement remplis.");
        }
    }

    private void updateChart(SimulationPlacement.ResultatSimulation resultat) {
        evolutionChart.getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Évolution du capital");

        BigDecimal montantInitial = resultat.getMontantInitial();
        BigDecimal tauxInteret = resultat.getTauxInteret().divide(BigDecimal.valueOf(100));
        boolean interetsComposes = interetsComposesCheck.isSelected();

        // Calcul des points pour chaque année
        for (int annee = 0; annee <= resultat.getDureeAnnees(); annee++) {
            BigDecimal montant;
            if (interetsComposes) {
                montant = montantInitial.multiply(
                    BigDecimal.ONE.add(tauxInteret).pow(annee)
                );
            } else {
                montant = montantInitial.add(
                    montantInitial.multiply(tauxInteret).multiply(BigDecimal.valueOf(annee))
                );
            }
            series.getData().add(new XYChart.Data<>(annee, montant.doubleValue()));
        }

        evolutionChart.getData().add(series);
    }
}
