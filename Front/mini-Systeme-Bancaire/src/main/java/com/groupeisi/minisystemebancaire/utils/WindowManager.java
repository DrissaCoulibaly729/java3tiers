package gm.rahmanproperties.optibank.utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

public class WindowManager {
    private static final Logger logger = LogManager.getLogger(WindowManager.class);

    public static void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture de la fenêtre " + fxmlPath, e);
            showError("Erreur", "Impossible d'ouvrir la fenêtre", e.getMessage());
        }
    }

    public static void closeWindow() {
        Platform.runLater(() -> {
            Stage stage = (Stage) Stage.getWindows().stream()
                    .filter(Window::isShowing)
                    .findFirst()
                    .orElse(null);
            if (stage != null) {
                stage.close();
            }
        });
    }
    public static void closedAllAndOpenWindow(String fxmlPath, String title) {
        Platform.runLater(() -> {
            // Fermer toutes les fenêtres ouvertes
            for (Stage stage : Stage.getWindows().stream()
                    .filter(window -> window instanceof Stage)
                    .map(window -> (Stage) window)
                    .toList()) {
                stage.close();
            }

            // Ouvrir la nouvelle fenêtre
            try {
                FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                logger.error("Erreur lors de l'ouverture de la fenêtre " + fxmlPath, e);
                showError("Erreur", "Impossible d'ouvrir la fenêtre", e.getMessage());
            }
        });
    }

    public static void openModalWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture de la fenêtre modale " + fxmlPath, e);
            showError("Erreur", "Impossible d'ouvrir la fenêtre", e.getMessage());
        }
    }

    public static void showError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Fermer automatiquement après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    if (alert.isShowing()) {
                        Platform.runLater(alert::close);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            alert.show();
        });
    }

    public static void showConfirmation(String title, String message, String s, Runnable onConfirm) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK && onConfirm != null) {
                onConfirm.run();
            }
        });
    }

    public static void showFxPopupSuccess(String message) {
        Platform.runLater(() -> {
            Popup.showSuccessMessage(message);
        });
    }

    public static void showFxPopupError(String message) {
        Platform.runLater(() -> {
            Popup.showErrorMessage(message);
        });
    }

    public static void showFxPopup(String message) {}

    public static <T> T getController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(fxmlPath));
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du contrôleur {}", fxmlPath, e);
            showError("Erreur", "Impossible de charger le contrôleur", e.getMessage());
            return null;
        }
    }
}
