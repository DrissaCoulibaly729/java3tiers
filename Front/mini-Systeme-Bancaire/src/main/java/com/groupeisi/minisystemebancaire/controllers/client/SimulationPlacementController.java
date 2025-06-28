package com.groupeisi.minisystemebancaire.controllers.client;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.groupeisi.minisystemebancaire.utils.SimulationPlacement;
import com.groupeisi.minisystemebancaire.utils.CurrencyFormatter;
import com.groupeisi.minisystemebancaire.utils.ValidationUtils;
import com.groupeisi.minisystemebancaire.utils.WindowManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class SimulationPlacementController implements Initializable {

    // Formulaire de simulation
    @FXML private JFXTextField montantInitialField;
    @FXML private Slider dureeAnneesSlider;
    @FXML private JFXTextField tauxInteretField;
    @FXML private JFXCheckBox interetsComposesCheck;
    @FXML private JFXButton simulerButton;
    @FXML private JFXButton reinitialiserButton;

    // Labels pour les sliders
    @FXML private Label dureeLabel;

    // Résultats
    @FXML private VBox resultatContainer;
    @FXML private Label montantFinalLabel;
    @FXML private Label interetsGeneresLabel;
    @FXML private Label tauxEffectifLabel;
    @FXML private Label dureeAffichageLabel;

    // Graphique d'évolution
    @FXML private LineChart<Number, Number> evolutionChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    // Table d'évolution annuelle
    @FXML private TableView<EvolutionAnnuelle> evolutionTable;
    @FXML private TableColumn<EvolutionAnnuelle, Integer> anneeCol;
    @FXML private TableColumn<EvolutionAnnuelle, String> capitalCol;
    @FXML private TableColumn<EvolutionAnnuelle, String> interetsCol;
    @FXML private TableColumn<EvolutionAnnuelle, String> totalCol;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("fr", "SN"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerInterface();
        configurerValidation();
        configurerBoutons();
        configurerTable();
        configurerGraphique();
        initialiserValeurs();
    }

    private void configurerInterface() {
        // Configuration du slider de durée
        if (dureeAnneesSlider != null) {
            dureeAnneesSlider.setMin(1);
            dureeAnneesSlider.setMax(30);
            dureeAnneesSlider.setValue(5);
            dureeAnneesSlider.setMajorTickUnit(5);
            dureeAnneesSlider.setMinorTickCount(4);
            dureeAnneesSlider.setShowTickLabels(true);
            dureeAnneesSlider.setShowTickMarks(true);
            dureeAnneesSlider.setSnapToTicks(true);

            // Listener pour mettre à jour le label
            dureeAnneesSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int duree = newVal.intValue();
                if (dureeLabel != null) {
                    dureeLabel.setText(duree + (duree > 1 ? " ans" : " an"));
                }
                // Recalculer automatiquement si les autres champs sont remplis
                if (ValidationUtils.isValidAmount(montantInitialField.getText()) &&
                        ValidationUtils.isValidAmount(tauxInteretField.getText())) {
                    calculerSimulation();
                }
            });
        }

        // Valeurs par défaut
        if (interetsComposesCheck != null) {
            interetsComposesCheck.setSelected(true);
        }
    }

    private void configurerValidation() {
        // Validation du montant initial
        if (montantInitialField != null) {
            montantInitialField.textProperty().addListener((obs, old, newValue) -> {
                if (!ValidationUtils.isValidAmount(newValue) && !newValue.isEmpty()) {
                    montantInitialField.setText(old);
                } else if (ValidationUtils.isValidAmount(newValue) &&
                        ValidationUtils.isValidAmount(tauxInteretField.getText())) {
                    calculerSimulation();
                }
            });
        }

        // Validation du taux d'intérêt
        if (tauxInteretField != null) {
            tauxInteretField.textProperty().addListener((obs, old, newValue) -> {
                if (!newValue.matches("\\d*(\\.\\d*)?") && !newValue.isEmpty()) {
                    tauxInteretField.setText(old);
                } else if (ValidationUtils.isValidAmount(newValue) &&
                        ValidationUtils.isValidAmount(montantInitialField.getText())) {
                    calculerSimulation();
                }
            });
        }
    }

    private void configurerBoutons() {
        if (simulerButton != null) {
            simulerButton.setOnAction(e -> calculerSimulation());
        }
        if (reinitialiserButton != null) {
            reinitialiserButton.setOnAction(e -> reinitialiserFormulaire());
        }
        if (interetsComposesCheck != null) {
            interetsComposesCheck.setOnAction(e -> {
                if (ValidationUtils.isValidAmount(montantInitialField.getText()) &&
                        ValidationUtils.isValidAmount(tauxInteretField.getText())) {
                    calculerSimulation();
                }
            });
        }
    }

    private void configurerTable() {
        if (anneeCol != null) {
            anneeCol.setCellValueFactory(new PropertyValueFactory<>("annee"));
        }
        if (capitalCol != null) {
            capitalCol.setCellValueFactory(new PropertyValueFactory<>("capital"));
        }
        if (interetsCol != null) {
            interetsCol.setCellValueFactory(new PropertyValueFactory<>("interets"));
        }
        if (totalCol != null) {
            totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        }
    }

    private void configurerGraphique() {
        if (evolutionChart != null) {
            evolutionChart.setTitle("Évolution du placement");
            evolutionChart.setCreateSymbols(true);
            evolutionChart.setLegendVisible(true);
        }

        if (xAxis != null) {
            xAxis.setLabel("Années");
        }
        if (yAxis != null) {
            yAxis.setLabel("Montant (FCFA)");
        }
    }

    private void initialiserValeurs() {
        if (montantInitialField != null) {
            montantInitialField.setText("1000000");
        }
        if (tauxInteretField != null) {
            tauxInteretField.setText("5.0");
        }
        if (dureeLabel != null) {
            dureeLabel.setText("5 ans");
        }
    }

    @FXML
    private void calculerSimulation() {
        if (!validerChamps()) return;

        try {
            // Récupérer les valeurs
            BigDecimal montantInitial = new BigDecimal(montantInitialField.getText());
            double tauxAnnuel = Double.parseDouble(tauxInteretField.getText());
            int dureeAnnees = (int) dureeAnneesSlider.getValue();
            boolean interetsComposes = interetsComposesCheck.isSelected();

            // Calculer la simulation
            SimulationPlacement.ResultatSimulation resultat =
                    SimulationPlacement.calculerPlacement(montantInitial, tauxAnnuel, dureeAnnees, interetsComposes);

            // Afficher les résultats
            afficherResultats(resultat);

            // Mettre à jour le graphique
            mettreAJourGraphique(resultat);

            // Mettre à jour la table
            mettreAJourTable(resultat, montantInitial);

        } catch (NumberFormatException e) {
            WindowManager.showError("Erreur",
                    "Erreur de calcul",
                    "Veuillez vérifier les valeurs saisies.");
        }
    }

    private boolean validerChamps() {
        if (!ValidationUtils.isNotEmpty(montantInitialField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Montant initial manquant",
                    "Veuillez saisir le montant initial.");
            montantInitialField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isValidAmount(montantInitialField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Montant initial invalide",
                    "Veuillez saisir un montant valide.");
            montantInitialField.requestFocus();
            return false;
        }

        if (!ValidationUtils.isNotEmpty(tauxInteretField.getText())) {
            WindowManager.showError("Erreur de validation",
                    "Taux d'intérêt manquant",
                    "Veuillez saisir le taux d'intérêt annuel.");
            tauxInteretField.requestFocus();
            return false;
        }

        try {
            double taux = Double.parseDouble(tauxInteretField.getText());
            if (taux < 0 || taux > 50) {
                WindowManager.showError("Erreur de validation",
                        "Taux d'intérêt invalide",
                        "Le taux doit être compris entre 0 et 50%.");
                tauxInteretField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            WindowManager.showError("Erreur de validation",
                    "Format de taux invalide",
                    "Veuillez saisir un taux numérique valide.");
            tauxInteretField.requestFocus();
            return false;
        }

        BigDecimal montant = new BigDecimal(montantInitialField.getText());
        if (montant.compareTo(new BigDecimal("1000")) < 0) {
            WindowManager.showError("Erreur de validation",
                    "Montant trop faible",
                    "Le montant minimum est de 1 000 FCFA.");
            montantInitialField.requestFocus();
            return false;
        }

        return true;
    }

    private void afficherResultats(SimulationPlacement.ResultatSimulation resultat) {
        if (montantFinalLabel != null) {
            montantFinalLabel.setText(CurrencyFormatter.format(resultat.getMontantFinal().doubleValue()));
        }
        if (interetsGeneresLabel != null) {
            interetsGeneresLabel.setText(CurrencyFormatter.format(resultat.getInteretsGeneres().doubleValue()));
        }
        if (tauxEffectifLabel != null) {
            double tauxEffectif = resultat.getInteretsGeneres()
                    .divide(new BigDecimal(montantInitialField.getText()), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
            tauxEffectifLabel.setText(String.format("%.2f%%", tauxEffectif));
        }
        if (dureeAffichageLabel != null) {
            int duree = (int) dureeAnneesSlider.getValue();
            dureeAffichageLabel.setText(duree + (duree > 1 ? " ans" : " an"));
        }

        // Afficher le conteneur de résultats
        if (resultatContainer != null) {
            resultatContainer.setVisible(true);
        }
    }

    private void mettreAJourGraphique(SimulationPlacement.ResultatSimulation resultat) {
        if (evolutionChart == null) return;

        evolutionChart.getData().clear();

        // Série principale - évolution du capital
        XYChart.Series<Number, Number> serie = new XYChart.Series<>();
        serie.setName(interetsComposesCheck.isSelected() ? "Intérêts composés" : "Intérêts simples");

        BigDecimal[] evolution = resultat.getEvolutionAnnuelle();
        for (int i = 0; i < evolution.length; i++) {
            serie.getData().add(new XYChart.Data<>(i, evolution[i].doubleValue()));
        }

        evolutionChart.getData().add(serie);
    }

    private void mettreAJourTable(SimulationPlacement.ResultatSimulation resultat, BigDecimal montantInitial) {
        if (evolutionTable == null) return;

        List<EvolutionAnnuelle> donnees = new ArrayList<>();
        BigDecimal[] evolution = resultat.getEvolutionAnnuelle();

        for (int i = 0; i < evolution.length; i++) {
            BigDecimal capital = i == 0 ? montantInitial : evolution[i - 1];
            BigDecimal interets = evolution[i].subtract(capital);

            donnees.add(new EvolutionAnnuelle(
                    i,
                    CurrencyFormatter.format(capital.doubleValue()),
                    CurrencyFormatter.format(interets.doubleValue()),
                    CurrencyFormatter.format(evolution[i].doubleValue())
            ));
        }

        evolutionTable.setItems(FXCollections.observableArrayList(donnees));
    }

    @FXML
    private void reinitialiserFormulaire() {
        if (montantInitialField != null) montantInitialField.setText("1000000");
        if (tauxInteretField != null) tauxInteretField.setText("5.0");
        if (dureeAnneesSlider != null) {
            dureeAnneesSlider.setValue(5);
            if (dureeLabel != null) dureeLabel.setText("5 ans");
        }
        if (interetsComposesCheck != null) interetsComposesCheck.setSelected(true);

        // Masquer les résultats
        if (resultatContainer != null) {
            resultatContainer.setVisible(false);
        }

        // Vider les résultats
        if (montantFinalLabel != null) montantFinalLabel.setText("-");
        if (interetsGeneresLabel != null) interetsGeneresLabel.setText("-");
        if (tauxEffectifLabel != null) tauxEffectifLabel.setText("-");

        // Vider le graphique et la table
        if (evolutionChart != null) evolutionChart.getData().clear();
        if (evolutionTable != null) evolutionTable.getItems().clear();
    }

    @FXML
    private void exporterResultats() {
        // TODO: Implémenter l'export des résultats en PDF/Excel
        WindowManager.showWarning("Fonctionnalité",
                "Export en cours de développement",
                "L'export des résultats sera disponible prochainement.");
    }

    // Classe pour la table d'évolution
    public static class EvolutionAnnuelle {
        private final Integer annee;
        private final String capital;
        private final String interets;
        private final String total;

        public EvolutionAnnuelle(Integer annee, String capital, String interets, String total) {
            this.annee = annee;
            this.capital = capital;
            this.interets = interets;
            this.total = total;
        }

        public Integer getAnnee() { return annee; }
        public String getCapital() { return capital; }
        public String getInterets() { return interets; }
        public String getTotal() { return total; }
    }
}