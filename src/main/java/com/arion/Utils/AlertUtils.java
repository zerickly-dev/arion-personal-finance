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
        Alert alert = createSimpleAlert(Alert.AlertType.INFORMATION, title, message);
        alert.showAndWait();
    }

    public static void showErrorAlert(String title, String message) {
        Alert alert = createSimpleAlert(Alert.AlertType.ERROR, title, message);
        alert.showAndWait();
    }

    public static void showWarningAlert(String title, String message) {
        Alert alert = createSimpleAlert(Alert.AlertType.WARNING, title, message);
        alert.showAndWait();
    }

    public static boolean showConfirmationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Configurar estilo básico
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                AlertUtils.class.getResource("/Css/AlertStyles.css").toExternalForm());

        // Configurar botones específicamente para confirmación
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);

        if (okButton != null) {
            okButton.setText("Confirmar");
            okButton.getStyleClass().add("default-button");
            okButton.setPrefWidth(120);
        }

        if (cancelButton != null) {
            cancelButton.setText("Cancelar");
            cancelButton.getStyleClass().add("cancel-button");
            cancelButton.setPrefWidth(120);
        }

        // Ajustar el tamaño del diálogo
        dialogPane.setPrefWidth(400);
        dialogPane.setPrefHeight(200);

        // Centrar la ventana
        alert.setOnShown(e -> {
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.setResizable(false);
            stage.centerOnScreen();
        });

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static Alert createSimpleAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Aplicar hoja de estilos CSS
        DialogPane dialogPane = alert.getDialogPane();

        try {
            String cssPath = AlertUtils.class.getResource("/Css/AlertStyles.css").toExternalForm();
            dialogPane.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Error cargando estilos: " + e.getMessage());
        }

        // Configurar el botón OK
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.getStyleClass().add("default-button");
            okButton.setPrefWidth(120);

            switch (alertType) {
                case INFORMATION:
                    okButton.setText("Entendido");
                    break;
                case ERROR:
                    okButton.setText("Cerrar");
                    break;
                case WARNING:
                    okButton.setText("Aceptar");
                    break;
                default:
                    okButton.setText("OK");
            }
        }

        // Ajustar el tamaño del diálogo
        dialogPane.setPrefWidth(400);
        dialogPane.setPrefHeight(180);

        // Centrar la ventana
        alert.setOnShown(e -> {
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.setResizable(false);
            stage.centerOnScreen();
        });

        return alert;
    }
}
