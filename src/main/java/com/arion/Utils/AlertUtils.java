package com.arion.Utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class AlertUtils {

    public static void showSuccessAlert(String title, String message) {
        Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, title, message);
        alert.showAndWait();
    }

    public static void showErrorAlert(String title, String message) {
        Alert alert = createStyledAlert(Alert.AlertType.ERROR, title, message);
        alert.showAndWait();
    }

    public static void showWarningAlert(String title, String message) {
        Alert alert = createStyledAlert(Alert.AlertType.WARNING, title, message);
        alert.showAndWait();
    }

    public static boolean showConfirmationAlert(String title, String message) {
        Alert alert = createStyledAlert(Alert.AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static Alert createStyledAlert(Alert.AlertType alertType, String title, String message) {
        try {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Obtener el DialogPane para aplicar estilos
            DialogPane dialogPane = alert.getDialogPane();

            // Aplicar hoja de estilos CSS
            try {
                String cssPath = AlertUtils.class.getResource("/Css/AlertStyles.css").toExternalForm();
                dialogPane.getStylesheets().clear();
                dialogPane.getStylesheets().add(cssPath);
            } catch (Exception e) {
                System.err.println("Error cargando estilos: " + e.getMessage());
            }

            // Configurar contenido centrado
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setAlignment(Pos.CENTER);
            messageLabel.setMaxWidth(Double.MAX_VALUE);
            messageLabel.setStyle("-fx-text-alignment: center;");

            // Contenedor para el mensaje y controles
            VBox contentBox = new VBox(10);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.getChildren().add(messageLabel);

            // Reemplazar contenido por defecto
            dialogPane.setContent(contentBox);

            // Centrar los botones de forma segura
            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setSpacing(15);

            // Aplicar estilo a los botones solo si existen
            if (alertType == Alert.AlertType.CONFIRMATION) {
                Node okButton = dialogPane.lookupButton(ButtonType.OK);
                Node cancelButton = dialogPane.lookupButton(ButtonType.CANCEL);

                if (okButton != null && okButton instanceof Button) {
                    Button btn = (Button) okButton;
                    btn.getStyleClass().add("default-button");
                    buttonBox.getChildren().add(btn);
                }

                if (cancelButton != null && cancelButton instanceof Button) {
                    Button btn = (Button) cancelButton;
                    btn.getStyleClass().add("cancel-button");
                    buttonBox.getChildren().add(btn);
                }

                if (!buttonBox.getChildren().isEmpty()) {
                    // Solo agregar los botones personalizados si hay alguno
                    VBox.setMargin(buttonBox, new Insets(20, 0, 0, 0));
                    contentBox.getChildren().add(buttonBox);

                    // Ocultar los botones originales solo si se han movido a nuestra caja personalizada
                    ButtonBar originalButtonBar = (ButtonBar) dialogPane.lookup(".button-bar");
                    if (originalButtonBar != null) {
                        originalButtonBar.setVisible(false);
                        originalButtonBar.setManaged(false);
                    }
                }
            }

            // Limpiar clases existentes y aplicar nuevas
            dialogPane.getStyleClass().clear();
            dialogPane.getStyleClass().add("dialog-pane");

            // Aplicar clase específica según el tipo de alerta
            switch (alertType) {
                case INFORMATION:
                    dialogPane.getStyleClass().add("success-alert");
                    break;
                case ERROR:
                    dialogPane.getStyleClass().add("error-alert");
                    break;
                case WARNING:
                    dialogPane.getStyleClass().add("warning-alert");
                    break;
                case CONFIRMATION:
                    dialogPane.getStyleClass().add("confirmation-alert");
                    break;
            }

            // Remover el icono por defecto de JavaFX
            alert.setGraphic(null);

            // Configurar el Stage después de que el Alert se haya mostrado
            alert.setOnShown(e -> {
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                if (stage != null) {
                    stage.setResizable(false);
                    stage.centerOnScreen();
                }
            });

            // Configurar tamaño del diálogo
            dialogPane.setPrefWidth(400);
            dialogPane.setMinWidth(350);
            dialogPane.setMaxWidth(450);
            dialogPane.setPrefHeight(200);
            dialogPane.setMinHeight(180);

            return alert;
        } catch (Exception e) {
            // En caso de error, devuelve una alerta básica sin personalización
            System.err.println("Error al crear alerta personalizada: " + e.getMessage());
            e.printStackTrace();

            Alert fallbackAlert = new Alert(alertType);
            fallbackAlert.setTitle(title);
            fallbackAlert.setHeaderText(null);
            fallbackAlert.setContentText(message);
            return fallbackAlert;
        }
    }
}
