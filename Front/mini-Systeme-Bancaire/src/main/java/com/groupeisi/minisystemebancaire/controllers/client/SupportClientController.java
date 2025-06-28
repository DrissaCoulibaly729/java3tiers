package com.groupeisi.minisystemebancaire.controllers.client;

import com.jfoenix.controls.*;
import gm.rahmanproperties.optibank.config.ApiConfig;
import gm.rahmanproperties.optibank.dtos.TicketSupportDTo;
import gm.rahmanproperties.optibank.utils.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class SupportClientController implements Initializable {
    @FXML
    private JFXTextArea descriptionField;
    @FXML private JFXTextField sujetField;
    @FXML private JFXListView<VBox> ticketsListView;
    @FXML private JFXButton envoyerButton;
    @FXML private JFXSpinner loadingSpinner;

    private final RestTemplate restTemplate = new RestTemplate();
    private String clientId;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupValidation();
        loadingSpinner.setVisible(false);
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
        chargerTickets();
    }

    private void setupValidation() {
//        sujetField.setValidators(new JFXValidator<String>() {
//            @Override
//            protected void eval() {
//                if (srcControl.get() == null || srcControl.get().trim().isEmpty()) {
//                    hasErrors.set(true);
//                    setMessage("Le sujet est requis");
//                }
//            }
//        });

//        descriptionField.setValidators(new JFXValidator<String>() {
//            @Override
//            protected void eval() {
//                if (srcControl.get() == null || srcControl.get().trim().isEmpty()) {
//                    hasErrors.set(true);
//                    setMessage("La description est requise");
//                }
//            }
//        });
    }

    @FXML
    private void envoyerTicket() {
        if (!validateFields()) return;

        showLoading(true);
        TicketSupportDTo newTicket = new TicketSupportDTo();
        newTicket.setSujet(sujetField.getText());
        newTicket.setDescription(descriptionField.getText());
//        newTicket.setClientId(clientId);
//        newTicket.setDateOuverture(LocalDateTime.now());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TicketSupportDTo> request = new HttpEntity<>(newTicket, headers);

        new Thread(() -> {
            try {
                ResponseEntity<TicketSupportDTo> response = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/support/tickets",
                    HttpMethod.POST,
                    request,
                    TicketSupportDTo.class
                );

                Platform.runLater(() -> {
                    if (response.getBody() != null) {
                        WindowManager.showSuccess("Succès", "Votre ticket a été envoyé");
//                        NotificationService.notifierTicketSupport(
//                            response.getBody().getClientEmail(),
//                            response.getBody().getId(),
//                            response.getBody().getStatut().toString()
//                        );
                        clearFields();
                        chargerTickets();
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    WindowManager.showError("Erreur", "Impossible d'envoyer le ticket", e.getMessage());
                });
            }
        }).start();
    }

    private void chargerTickets() {
        showLoading(true);
        new Thread(() -> {
            try {
                ResponseEntity<List<TicketSupportDTo>> response = restTemplate.exchange(
                    ApiConfig.getApiUrl() + "/support/tickets/client/" + clientId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
                );

                Platform.runLater(() -> {
                    if (response.getBody() != null) {
                        ticketsListView.getItems().clear();
                        response.getBody().forEach(this::addTicketToList);
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    WindowManager.showError("Erreur", "Impossible de charger les tickets", e.getMessage());
                });
            }
        }).start();
    }

    private void addTicketToList(TicketSupportDTo ticket) {
        VBox ticketBox = new VBox(5);
        ticketBox.getStyleClass().add("ticket-item");

        Label sujetLabel = new Label(ticket.getSujet());
        sujetLabel.getStyleClass().add("ticket-sujet");

//        Label dateLabel = new Label(ticket.getDateOuverture().format(DATE_FORMATTER));
//        dateLabel.getStyleClass().add("ticket-date");

        Label statutLabel = new Label(ticket.getStatut().toString());
        statutLabel.getStyleClass().addAll("ticket-statut", "statut-" + ticket.getStatut().toString().toLowerCase());

        Label descriptionLabel = new Label(ticket.getDescription());
        descriptionLabel.getStyleClass().add("ticket-description");
        descriptionLabel.setWrapText(true);

//        ticketBox.getChildren().addAll(sujetLabel, dateLabel, statutLabel, descriptionLabel);
        ticketsListView.getItems().add(ticketBox);
    }

    private boolean validateFields() {
        return sujetField.validate() && descriptionField.validate();
    }

    private void clearFields() {
        sujetField.clear();
        descriptionField.clear();
    }

    private void showLoading(boolean show) {
        loadingSpinner.setVisible(show);
        envoyerButton.setDisable(show);
    }
}
