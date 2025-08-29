package com.arion.Controller;

import com.arion.Model.Transaction;
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

    private Transaction transactionToEdit; // Variable para guardar la transacción que se está editando
    private FormType currentFormType;
    private ObservableList<Transaction> transactionsList; // Lista de transacciones del dashboard
    private DashboardViewController dashboardController; // Referencia al controlador del dashboard

    @FXML
    private void initialize() {
        // Establecer fecha por defecto a hoy
        datePicker.setValue(LocalDate.now());
    }

    // Método para configurar la UI según sea Ingreso o Gasto
    public void configureFor(FormType type) {
        this.currentFormType = type;

        if (type == FormType.INCOME) {
            formTitleLabel.setText("Add Income");
            formIcon.setContent("M12 8l-6 6 1.41 1.41L12 10.83l4.59 4.58L18 14z"); // Flecha hacia arriba
            formIcon.getStyleClass().setAll("icon-form", "icon-up");
            categoryComboBox.setItems(FXCollections.observableArrayList(
                "Salary", "Bonus", "Freelance", "Investment", "Gift", "Other"));
            saveButton.setText("Save Income");
        } else {
            formTitleLabel.setText("Add Expense");
            formIcon.setContent("M12 16l-6-6 1.41-1.41L12 13.17l4.59-4.58L18 10z"); // Flecha hacia abajo
            formIcon.getStyleClass().setAll("icon-form", "icon-down");
            categoryComboBox.setItems(FXCollections.observableArrayList(
                "Housing", "Food", "Transportation", "Entertainment", "Utilities", "Healthcare", "Other"));
            saveButton.setText("Save Expense");
        }

    }

    // Método para configurar con lista de transacciones (para nuevas transacciones)
    public void configureFor(FormType type, ObservableList<Transaction> transactionsList) {
        this.transactionsList = transactionsList;
        configureFor(type);
    }

    // Método para configurar con referencia al dashboard controller
    public void setDashboardController(DashboardViewController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void populateForm(Transaction transaction) {
        this.transactionToEdit = transaction;

        // Configura el formulario (Income o Expense) basado en el tipo de transacción
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

            if (transactionToEdit != null) {
                // Actualizar transacción existente
                transactionToEdit.setAmount(amount);
                transactionToEdit.setCategory(category);
                transactionToEdit.setDate(date);
                transactionToEdit.setNote(note);
                transactionToEdit.setType(type);

                System.out.println("Transacción actualizada: " + transactionToEdit.toString());
            } else {
                // Crear nueva transacción
                Transaction newTransaction = new Transaction(
                    category, // description = category por ahora
                    category,
                    date,
                    amount,
                    type,
                    note
                );

                // Agregar la nueva transacción a la lista del dashboard
                if (dashboardController != null) {
                    dashboardController.addTransaction(newTransaction);
                } else if (transactionsList != null) {
                    transactionsList.add(newTransaction);
                }

                System.out.println("Nueva transacción creada: " + newTransaction.toString());
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid amount.");
        } catch (Exception e) {
            showAlert("Error", "An error occurred while saving the transaction: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        if (amountField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Please enter an amount.");
            amountField.requestFocus();
            return false;
        }

        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showAlert("Validation Error", "Amount must be greater than 0.");
                amountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Please enter a valid number for amount.");
            amountField.requestFocus();
            return false;
        }

        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().trim().isEmpty()) {
            showAlert("Validation Error", "Please select a category.");
            categoryComboBox.requestFocus();
            return false;
        }

        if (datePicker.getValue() == null) {
            showAlert("Validation Error", "Please select a date.");
            datePicker.requestFocus();
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}