package com.arion.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class TransactionFormController {

    // Enum para definir el tipo de formulario
    public enum FormType {
        INCOME, EXPENSE
    }

    @FXML private VBox rootPane;
    @FXML private Label formTitleLabel;
    @FXML private SVGPath formIcon;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextArea noteTextArea;
    @FXML private Button saveButton;

    // Método para configurar la UI según sea Ingreso o Gasto
    public void configureFor(FormType type) {
        if (type == FormType.INCOME) {
            formTitleLabel.setText("Add Income");
            formIcon.setContent("M12 8l-6 6 1.41 1.41L12 10.83l4.59 4.58L18 14z"); // Flecha hacia arriba
            formIcon.getStyleClass().setAll("icon-form", "icon-up");
            categoryComboBox.setItems(FXCollections.observableArrayList("Salary", "Bonus", "Freelance", "Other"));
            saveButton.setText("Save Income");
        } else {
            formTitleLabel.setText("Add Expense");
            formIcon.setContent("M12 16l-6-6 1.41-1.41L12 13.17l4.59-4.58L18 10z"); // Flecha hacia abajo
            formIcon.getStyleClass().setAll("icon-form", "icon-down");
            categoryComboBox.setItems(FXCollections.observableArrayList("Housing", "Food", "Transportation", "Entertainment", "Utilities", "Other"));
            saveButton.setText("Save Expense");
        }
    }

    @FXML
    private void save() {
        // Aquí iría la lógica para guardar los datos
        System.out.println("Guardando datos:");
        System.out.println("Amount: " + amountField.getText());
        System.out.println("Category: " + categoryComboBox.getValue());
        System.out.println("Date: " + datePicker.getValue());
        System.out.println("Note: " + noteTextArea.getText());

        closeWindow();
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        // Obtiene la ventana (Stage) actual y la cierra
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}