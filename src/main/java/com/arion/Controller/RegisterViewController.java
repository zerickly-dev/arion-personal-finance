package com.arion.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;

public class RegisterViewController {
    @FXML
    private void Register (ActionEvent event) {
        try {
            // Carga el nuevo FXML con un nuevo loader cada vez
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/LoginView.fxml"));
            Parent nuevaEscena = loader.load();

            // Obtiene la ventana actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Obtiene el tamaño de la pantalla
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Crea la nueva escena con tamaño igual al de la pantalla
            Scene scene = new Scene(nuevaEscena, screenBounds.getWidth(), screenBounds.getHeight());

            // Configura la posición y tamaño antes de mostrar
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            // Establece la escena
            stage.setScene(scene);

            // Configura maximizado sin animación
            stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void LoginView (ActionEvent event) {
        try {
            // Carga el nuevo FXML con un nuevo loader cada vez
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/LoginView.fxml"));
            Parent nuevaEscena = loader.load();

            // Obtiene la ventana actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Obtiene el tamaño de la pantalla
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Crea la nueva escena con tamaño igual al de la pantalla
            Scene scene = new Scene(nuevaEscena, screenBounds.getWidth(), screenBounds.getHeight());

            // Configura la posición y tamaño antes de mostrar
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            // Establece la escena
            stage.setScene(scene);

            // Configura maximizado sin animación
            stage.setMaximized(true);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
