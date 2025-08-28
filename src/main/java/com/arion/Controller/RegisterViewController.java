package com.arion.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterViewController {
    @FXML
    private void Register (ActionEvent event) {
        try {
            // Carga el nuevo FXML
            Parent nuevaEscena = FXMLLoader.load(getClass().getResource("/Fxml/LoginView.fxml"));

            // Obtiene la ventana actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Cambia la escena
            stage.setScene(new Scene(nuevaEscena));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void LoginView (ActionEvent event) {
        try {
            // Carga el nuevo FXML
            Parent nuevaEscena = FXMLLoader.load(getClass().getResource("/Fxml/LoginView.fxml"));

            // Obtiene la ventana actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Cambia la escena
            stage.setScene(new Scene(nuevaEscena));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
