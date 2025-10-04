package com.arion.Utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
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

            System.out.println("CSS de alertas modernas cargado: " + cssPath);
        } catch (Exception e) {
            System.err.println("Error cargando estilos: " + e.getMessage());
            e.printStackTrace();
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

        // Remover el icono por defecto de JavaFX para un look más limpio
        alert.setGraphic(null);

        // Configurar el Stage después de que el Alert se haya mostrado
        alert.setOnShown(e -> {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            if (stage != null) {
                stage.setResizable(false);
                // Centrar la ventana en la pantalla
                stage.centerOnScreen();
            }
        });

        // Configurar tamaño del diálogo para el nuevo diseño
        dialogPane.setPrefWidth(400);
        dialogPane.setMinWidth(350);
        dialogPane.setMaxWidth(450);
        dialogPane.setPrefHeight(180);
        dialogPane.setMinHeight(150);

        return alert;
    }
}
