package com.arion.Utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(null); // Lo hacemos null para usar nuestro propio contenido

        // Obtener el DialogPane para aplicar estilos
        DialogPane dialogPane = alert.getDialogPane();

        // Crear contenedor personalizado
        VBox contentBox = new VBox(10);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(10, 10, 10, 10));

        // Agregar el mensaje en un Label personalizado
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setStyle("-fx-text-alignment: center;");

        contentBox.getChildren().add(messageLabel);

        // Reemplazar el contenido predeterminado con nuestro contenido personalizado
        dialogPane.setContent(contentBox);

        // Aplicar hoja de estilos CSS
        try {
            String cssPath = AlertUtils.class.getResource("/Css/AlertStyles.css").toExternalForm();
            dialogPane.getStylesheets().clear();
            dialogPane.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Error cargando estilos: " + e.getMessage());
            e.printStackTrace();
        }

        // Centrar los botones manualmente
        dialogPane.lookupButton(ButtonType.OK).getStyleClass().add("default-button");
        dialogPane.lookupButton(ButtonType.CANCEL).getStyleClass().add("cancel-button");

        // Acceder al contenedor de botones y forzar centrado
        Node buttonBar = dialogPane.lookup(".button-bar");
        if (buttonBar instanceof ButtonBar) {
            ButtonBar buttonBarControl = (ButtonBar) buttonBar;
            buttonBarControl.setButtonOrder(ButtonBar.BUTTON_ORDER_WINDOWS);
            buttonBarControl.setButtonMinWidth(100);

            // Forzar el centrado en el ButtonBar
            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setSpacing(10);

            // Obtener y reorganizar los botones
            for (Node button : dialogPane.getButtonTypes().stream()
                     .map(dialogPane::lookupButton)
                     .toArray(Node[]::new)) {
                buttonBox.getChildren().add(button);
                buttonBarControl.getButtons().remove(button);
            }

            // Aplicar configuración a los botones
            for (Node button : buttonBox.getChildren()) {
                HBox.setHgrow(button, Priority.ALWAYS);
                ((Button)button).setMaxWidth(Double.MAX_VALUE);
            }

            // Agregar el nuevo contenedor de botones al centro del diálogo
            contentBox.getChildren().add(buttonBox);
            VBox.setMargin(buttonBox, new Insets(20, 0, 0, 0));

            // Ocultar el ButtonBar original
            buttonBarControl.setVisible(false);
            buttonBarControl.setManaged(false);
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
    }
}
