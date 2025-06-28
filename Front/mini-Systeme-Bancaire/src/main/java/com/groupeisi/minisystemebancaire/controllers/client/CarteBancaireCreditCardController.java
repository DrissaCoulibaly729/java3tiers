package com.groupeisi.minisystemebancaire.controllers.client;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTooltip;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import gm.rahmanproperties.optibank.config.ApiConfig;
import gm.rahmanproperties.optibank.dtos.CarteBancaireDTo;
import gm.rahmanproperties.optibank.dtos.CompteDTo;
import gm.rahmanproperties.optibank.utils.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CarteBancaireCreditCardController implements Initializable {
    @FXML
    private VBox cardContainer;
    @FXML private Label cardNumberLabel;
    @FXML private Label cardExpiryLabel;
    @FXML private Label cardStatusLabel;
    @FXML private JFXButton requestCardButton;
    @FXML private JFXButton blockCardButton;
    @FXML private JFXButton unblockCardButton;
    @FXML private JFXSpinner loadingSpinner;
    @FXML private JFXTextField pinTextField;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = ApiConfig.getApiUrl() + "/api/cartes";
    private CarteBancaireDTo currentCard;
    private CompteDTo currentAccount;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUI();
        chargerCartesClient();
    }

    private void setupUI() {
        loadingSpinner.setVisible(false);
        blockCardButton.setDisable(true);
        unblockCardButton.setDisable(true);

        // Configuration des icônes des boutons
        requestCardButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.CREDIT_CARD_PLUS, "20px"));
        blockCardButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.LOCK, "20px"));
        unblockCardButton.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.LOCK_OPEN, "20px"));

        // Style des boutons
        requestCardButton.getStyleClass().add("button-primary");
        blockCardButton.getStyleClass().add("button-danger");
        unblockCardButton.getStyleClass().add("button-success");

        // Validation du code PIN
        pinTextField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d{0,4}")) {
                pinTextField.setText(old);
            }
        });

        // Ajout des tooltips
//        requestCardButton.setTooltip(new JFXTooltip("Demander une nouvelle carte"));
//        blockCardButton.setTooltip(new JFXTooltip("Bloquer la carte"));
//        unblockCardButton.setTooltip(new JFXTooltip("Débloquer la carte"));
    }

    public void setCompte(CompteDTo compte) {
        this.currentAccount = compte;
        loadCardData();
    }

    private void loadCardData() {
        if (currentAccount == null) return;

        showLoading(true);
        new Thread(() -> {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    API_URL + "/compte/" + currentAccount.getClientId(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
                );

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    WindowManager.showFxPopupError("Impossible de charger les données de la carte"+ e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void requestCard() {
        if (!validatePin()) return;

        showLoading(true);
        Map<String, Object> request = new HashMap<>();
        request.put("pin", pinTextField.getText());
        request.put("compteId", currentAccount.getClientId());

        new Thread(() -> {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(request, ApiConfig.createHeaders()),
                    new ParameterizedTypeReference<>() {}
                );

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    WindowManager.showError("Erreur", "Impossible de créer la carte", e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void blockCard() {
        if (currentCard == null) return;

        WindowManager.showConfirmation(
            "Confirmation",
            "Êtes-vous sûr de vouloir bloquer cette carte ?",
            "Cette action est irréversible.",
            () -> {
                showLoading(true);
                new Thread(() -> {
                    try {
                        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                            API_URL + "/" + currentCard.getId() + "/bloquer",
                            HttpMethod.POST,
                            null,
                            new ParameterizedTypeReference<>() {}
                        );
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showLoading(false);
                            WindowManager.showError("Erreur", "Impossible de bloquer la carte", e.getMessage());
                        });
                    }
                }).start();
            }
        );
    }

    @FXML
    private void unblockCard() {
        if (currentCard == null) return;

        WindowManager.showConfirmation(
            "Confirmation",
            "Êtes-vous sûr de vouloir débloquer cette carte ?",
            "Cette action nécessitera une validation supplémentaire.",
            () -> {
                showLoading(true);
                new Thread(() -> {
                    try {
                        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                            API_URL + "/" + currentCard.getId() + "/debloquer",
                            HttpMethod.POST,
                            null,
                            new ParameterizedTypeReference<>() {}
                        );
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showLoading(false);
                            WindowManager.showError("Erreur", "Impossible de débloquer la carte", e.getMessage());
                        });
                    }
                }).start();
            }
        );
    }

    private void updateCardDisplay() {
        if (currentCard != null) {
            cardNumberLabel.setText(formatCardNumber(currentCard.getNumero()));
            cardExpiryLabel.setText(currentCard.getDateExpiration().format(String.valueOf(DATE_FORMATTER)));
            cardStatusLabel.setText(currentCard.getStatut().toString());

//            blockCardButton.setDisable(currentCard.getStatut() != StatutCarte.ACTIVE);
//            unblockCardButton.setDisable(currentCard.getStatut() != StatutCarte.BLOQUEE);

            cardContainer.setVisible(true);
//            cardContainer.getStyleClass().setAll("card-container",
//                currentCard.getStatut() == StatutCarte.ACTIVE ? "card-active" : "card-blocked");
        } else {
            cardContainer.setVisible(false);
        }
    }

    private boolean validatePin() {
        String pin = pinTextField.getText();
        if (pin == null || !pin.matches("\\d{4}")) {
            WindowManager.showError("Erreur", "Code PIN invalide",
                "Le code PIN doit contenir exactement 4 chiffres.");
            return false;
        }
        return true;
    }

    private String formatCardNumber(String numero) {
        if (numero == null || numero.length() != 16) return "XXXX XXXX XXXX XXXX";
        return String.format("%s %s %s %s",
            numero.substring(0, 4),
            numero.substring(4, 8),
            numero.substring(8, 12),
            numero.substring(12, 16));
    }

    private void showLoading(boolean show) {
        loadingSpinner.setVisible(show);
        requestCardButton.setDisable(show);
//        blockCardButton.setDisable(show || currentCard == null || currentCard.getStatut() != StatutCarte.ACTIVE);
//        unblockCardButton.setDisable(show || currentCard == null || currentCard.getStatut() != StatutCarte.BLOQUEE);
    }

    private void chargerCartesClient() {
        Long compteId = 1L;
        try {
            CarteBancaireDTo[] cartes = restTemplate.getForObject(
                API_URL + "/compte/" + compteId,
                CarteBancaireDTo[].class
            );
            // TODO: Update UI with cards
        } catch (Exception e) {
            // TODO: Handle error
        }
    }

    @FXML
    private void signalerPerte(Long carteId) {
        try {
            restTemplate.postForObject(
                API_URL + "/" + carteId + "/signaler-perte",
                null,
                Void.class
            );
            // TODO: Update UI
        } catch (Exception e) {
            // TODO: Handle error
        }
    }

    @FXML
    private void changerCodePin(Long carteId, String ancienPin, String nouveauPin) {
        try {
            var request = Map.of(
                "ancienPin", ancienPin,
                "nouveauPin", nouveauPin
            );
            restTemplate.postForObject(
                API_URL + "/" + carteId + "/changer-code",
                request,
                Void.class
            );
            // TODO: Show success message
        } catch (Exception e) {
            // TODO: Handle error
        }
    }
}
