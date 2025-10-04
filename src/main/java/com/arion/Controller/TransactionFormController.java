package com.arion.Controller;

import com.arion.Model.Transaction;
import com.arion.Model.Budget;
import com.arion.Config.SessionManager;
import com.arion.Utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.YearMonth;

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
        formTitleLabel.setText(currentFormType == FormType.INCOME ? "Editar Ingreso" : "Editar Gasto");
        saveButton.setText("Actualizar");
    }

    // Método específico para configurar el formulario para edición
    public void configureForEdit(Transaction transaction) {
        populateForm(transaction);
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

            // NUEVA FUNCIONALIDAD: Verificar si el gasto va a rebasar un presupuesto
            if (type == Transaction.TransactionType.EXPENSE) {
                if (!checkBudgetBeforeSaving(amount, category, date)) {
                    return; // El usuario canceló la operación
                }
            }

            Transaction transaction;
            boolean success;
            int userId = SessionManager.getInstance().getCurrentUserId();

            if (transactionToEdit != null) {
                // Actualizar transacción existente
                transactionToEdit.setAmount(amount);
                transactionToEdit.setCategory(category);
                transactionToEdit.setDate(date);
                transactionToEdit.setNote(note);
                transactionToEdit.setType(type);
                success = transactionToEdit.update();
            } else {
                // Crear nueva transacción
                transaction = new Transaction(category, category, date, amount, type, note);
                success = transaction.save(userId);
            }

            if (success) {
                AlertUtils.showSuccessAlert("Éxito", "Transacción guardada correctamente");

                // Llamar callback para refrescar dashboard
                if (onTransactionSaved != null) {
                    onTransactionSaved.run();
                }

                closeWindow();
            } else {
                AlertUtils.showErrorAlert("Error", "No se pudo guardar la transacción");
            }

        } catch (NumberFormatException e) {
            AlertUtils.showErrorAlert("Error", "Por favor ingrese un monto válido");
        } catch (Exception e) {
            AlertUtils.showErrorAlert("Error", "Ocurrió un error al guardar la transacción: " + e.getMessage());
        }
    }

    /**
     * Verifica si el gasto va a rebasar un presupuesto establecido
     * @return true si el usuario confirma continuar o no hay presupuesto, false si cancela
     */
    private boolean checkBudgetBeforeSaving(double newExpenseAmount, String category, LocalDate date) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        YearMonth expenseMonth = YearMonth.from(date);

        // Obtener el presupuesto para esta categoría y mes
        Budget budget = Budget.getBudgetForCategoryAndMonth(userId, category, expenseMonth);

        if (budget == null) {
            return true; // No hay presupuesto definido, continuar normalmente
        }

        // Calcular el gasto actual en esta categoría
        double currentSpent = Budget.getSpentAmountForCategoryInMonth(userId, category, expenseMonth);

        // Calcular el gasto total después de agregar esta transacción
        double totalAfterExpense = currentSpent + newExpenseAmount;

        // Verificar si se va a rebasar el presupuesto
        if (totalAfterExpense > budget.getLimitAmount()) {
            double excess = totalAfterExpense - budget.getLimitAmount();

            String message = String.format(
                "⚠️ ADVERTENCIA DE PRESUPUESTO\n\n" +
                "Esta operación hará que rebase el presupuesto establecido para '%s'.\n\n" +
                "Presupuesto límite: $%.2f\n" +
                "Gastado actual: $%.2f\n" +
                "Nuevo gasto: $%.2f\n" +
                "Total después: $%.2f\n" +
                "Excedente: $%.2f\n\n" +
                "¿Desea continuar de todos modos?",
                category,
                budget.getLimitAmount(),
                currentSpent,
                newExpenseAmount,
                totalAfterExpense,
                excess
            );

            // Mostrar alerta de confirmación
            return AlertUtils.showConfirmationAlert("Presupuesto Excedido", message);
        }

        return true; // No se rebasa el presupuesto, continuar normalmente
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private boolean validateForm() {
        if (amountField.getText().trim().isEmpty()) {
            AlertUtils.showErrorAlert("Error de validación", "El monto es obligatorio");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                AlertUtils.showErrorAlert("Error de validación", "El monto debe ser mayor que cero");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showErrorAlert("Error de validación", "El monto debe ser un número válido");
            return false;
        }

        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty()) {
            AlertUtils.showErrorAlert("Error de validación", "La categoría es obligatoria");
            return false;
        }

        if (datePicker.getValue() == null) {
            AlertUtils.showErrorAlert("Error de validación", "La fecha es obligatoria");
            return false;
        }

        if (!SessionManager.getInstance().isLoggedIn()) {
            AlertUtils.showErrorAlert("Error de sesión", "No hay usuario autenticado");
            return false;
        }

        return true;
    }


    private void closeWindow() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}