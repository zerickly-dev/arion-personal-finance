package com.arion.Controller;

import com.arion.Model.Transaction;
import com.arion.Config.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import java.time.LocalDate;

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

    private Transaction transactionToEdit;
    private FormType currentFormType;
    private Runnable onTransactionSaved; // Callback para refrescar dashboard

    @FXML
    private void initialize() {
        // Establecer fecha por defecto a hoy
        datePicker.setValue(LocalDate.now());
    }

    // Método para configurar callback de guardado
    public void setOnTransactionSaved(Runnable callback) {
        this.onTransactionSaved = callback;
    }

    // Método para configurar la UI según sea Ingreso o Gasto
    public void configureFor(FormType type) {
        this.currentFormType = type;

        if (type == FormType.INCOME) {
            formTitleLabel.setText("Agregar Ingreso");
            formIcon.setContent("M12 8l-6 6 1.41 1.41L12 10.83l4.59 4.58L18 14z"); // Flecha hacia arriba
            formIcon.getStyleClass().setAll("icon-form", "icon-up");
            categoryComboBox.setItems(FXCollections.observableArrayList(
                "Salario", "Bonus", "Freelance", "Inversion", "Regalo", "Otros"));
            saveButton.setText("Agregar");
        } else {
            formTitleLabel.setText("Agregar Gasto");
            formIcon.setContent("M12 16l-6-6 1.41-1.41L12 13.17l4.59-4.58L18 10z"); // Flecha hacia abajo
            formIcon.getStyleClass().setAll("icon-form", "icon-down");
            categoryComboBox.setItems(FXCollections.observableArrayList(
                "Hogar", "Comida", "Transporte", "Entretenimiento", "Utiles", "Salud", "Otros"));
            saveButton.setText("Agregar");
        }
    }

    public void populateForm(Transaction transaction) {
        this.transactionToEdit = transaction;

        // Configura el formulario basado en el tipo de transacción
        configureFor(transaction.getType() == Transaction.TransactionType.INCOME ? FormType.INCOME : FormType.EXPENSE);

        // Llena los campos con los datos de la transacción
        amountField.setText(String.format("%.2f", transaction.getAmount()));
        categoryComboBox.setValue(transaction.getCategory());
        datePicker.setValue(transaction.getDate());
        noteTextArea.setText(transaction.getNote());

        // Cambiar título para indicar que es edición
        formTitleLabel.setText(currentFormType == FormType.INCOME ? "Edit Income" : "Edit Expense");
        saveButton.setText(currentFormType == FormType.INCOME ? "Update Income" : "Update Expense");
    }

    @FXML
    private void save() {
        if (!validateForm()) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText());
            String category = categoryComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String note = noteTextArea.getText().trim();
            Transaction.TransactionType type = currentFormType == FormType.INCOME ?
                Transaction.TransactionType.INCOME : Transaction.TransactionType.EXPENSE;

            Transaction transaction;
            boolean success;

            if (transactionToEdit != null) {
                // Actualizar transacción existente
                transactionToEdit.setAmount(amount);
                transactionToEdit.setCategory(category);
                transactionToEdit.setDate(date);
                transactionToEdit.setNote(note);
                transactionToEdit.setType(type);
                success = transactionToEdit.save();
            } else {
                // Crear nueva transacción
                transaction = new Transaction(category, category, date, amount, type, note);
                transaction.setUserId(SessionManager.getInstance().getCurrentUserId());
                success = transaction.save();
            }

            if (success) {
                showAlert("Éxito", "Transacción guardada correctamente", Alert.AlertType.INFORMATION);

                // Llamar callback para refrescar dashboard
                if (onTransactionSaved != null) {
                    onTransactionSaved.run();
                }

                closeWindow();
            } else {
                showAlert("Error", "No se pudo guardar la transacción");
            }

        } catch (NumberFormatException e) {
            showAlert("Error", "Por favor ingrese un monto válido");
        } catch (Exception e) {
            showAlert("Error", "Ocurrió un error al guardar la transacción: " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private boolean validateForm() {
        if (amountField.getText().trim().isEmpty()) {
            showAlert("Error de validación", "El monto es obligatorio");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showAlert("Error de validación", "El monto debe ser mayor que cero");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Error de validación", "El monto debe ser un número válido");
            return false;
        }

        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            showAlert("Error de validación", "La categoría es obligatoria");
            return false;
        }

        if (datePicker.getValue() == null) {
            showAlert("Error de validación", "La fecha es obligatoria");
            return false;
        }

        if (!SessionManager.getInstance().isLoggedIn()) {
            showAlert("Error de sesión", "No hay usuario autenticado");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void closeWindow() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}