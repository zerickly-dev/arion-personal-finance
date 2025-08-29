package com.arion.Controller;

import com.arion.Model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DashboardViewController implements Initializable {

    @FXML
    private PieChart expensesPieChart;

    @FXML
    private ListView<Transaction> transactionsListView;

    @FXML
    private Label totalIncomeLabel;

    @FXML
    private Label totalExpensesLabel;

    @FXML
    private Label netBalanceLabel;

    private ObservableList<Transaction> transactions;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupPieChart();
        setupTransactionList();
        updateSummaryLabels();
    }

    private void setupPieChart() {
        ObservableList<PieChart.Data> pieChartData =
                FXCollections.observableArrayList(
                        new PieChart.Data("Housing", 1200),
                        new PieChart.Data("Food", 800),
                        new PieChart.Data("Transportation", 550),
                        new PieChart.Data("Entertainment", 300),
                        new PieChart.Data("Utilities", 200),
                        new PieChart.Data("Other", 130));

        expensesPieChart.setData(pieChartData);
        expensesPieChart.setTitle(null); // Título ya está en la VBox
    }

    private void setupTransactionList() {
        // Crear transacciones de ejemplo usando el modelo unificado
        transactions = FXCollections.observableArrayList(
                new Transaction("Monthly Salary", "Salary", LocalDate.now().minusDays(1), 3200.00, Transaction.TransactionType.INCOME, "Regular monthly salary"),
                new Transaction("Grocery Shopping", "Food", LocalDate.now(), 85.50, Transaction.TransactionType.EXPENSE, "Weekly groceries at supermarket"),
                new Transaction("Freelance Project", "Freelance", LocalDate.now().minusDays(3), 450.00, Transaction.TransactionType.INCOME, "Web development project"),
                new Transaction("Gas Station", "Transportation", LocalDate.now().minusDays(2), 65.00, Transaction.TransactionType.EXPENSE, "Car fuel")
        );

        transactionsListView.setItems(transactions);

        // Celda personalizada para mostrar cada transacción
        transactionsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    VBox descriptionBox = new VBox();
                    Label categoryLabel = new Label(item.getCategory());
                    categoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                    Label dateLabel = new Label(item.getDateString());
                    dateLabel.setFont(Font.font("Arial", 12));
                    dateLabel.setStyle("-fx-text-fill: #666666;");

                    descriptionBox.getChildren().addAll(categoryLabel, dateLabel);

                    HBox amountBox = new HBox();
                    amountBox.setAlignment(Pos.CENTER_RIGHT);
                    HBox.setHgrow(amountBox, javafx.scene.layout.Priority.ALWAYS);

                    String amountStr = String.format("%.2f", item.getAmount());
                    String prefix = item.isIncome() ? "+" : "-";
                    Label amountLabel = new Label(prefix + "$" + amountStr);
                    amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    amountLabel.setStyle(item.isIncome() ? "-fx-text-fill: #2E7D32;" : "-fx-text-fill: #C62828;");

                    amountBox.getChildren().add(amountLabel);
                    hbox.getChildren().addAll(descriptionBox, amountBox);

                    setGraphic(hbox);
                }
            }
        });
    }



    private void updateSummaryLabels() {
        double totalIncome = transactions.stream()
                .filter(Transaction::isIncome)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(t -> !t.isIncome())
                .mapToDouble(Transaction::getAmount)
                .sum();

        double netBalance = totalIncome - totalExpenses;

        if (totalIncomeLabel != null) {
            totalIncomeLabel.setText(String.format("$%.2f", totalIncome));
        }
        if (totalExpensesLabel != null) {
            totalExpensesLabel.setText(String.format("$%.2f", totalExpenses));
        }
        if (netBalanceLabel != null) {
            netBalanceLabel.setText(String.format("$%.2f", netBalance));
        }
    }

    @FXML
    private void addIncome() {
        openTransactionForm(TransactionFormController.FormType.INCOME);
    }

    @FXML
    private void addExpense() {
        openTransactionForm(TransactionFormController.FormType.EXPENSE);
    }

    private void openTransactionForm(TransactionFormController.FormType formType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TransactionFormView.fxml"));
            Parent root = loader.load();

            TransactionFormController controller = loader.getController();
            controller.configureFor(formType);
            controller.setDashboardController(this); // Pasar referencia del dashboard

            Stage stage = new Stage();
            stage.setTitle(formType == TransactionFormController.FormType.INCOME ? "Add Income" : "Add Expense");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.setOnHidden(e -> {
                // Actualizar la vista cuando se cierre el formulario
                updateSummaryLabels();
                transactionsListView.refresh();
            });

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método público para añadir una nueva transacción (útil para integraciones futuras)
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        updateSummaryLabels();
    }

    // Método público para obtener todas las transacciones
    public ObservableList<Transaction> getTransactions() {
        return transactions;
    }

    @FXML
    private void onViewReportsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/ReportsView.fxml"));
            Parent root = loader.load();

            ReportsViewController controller = loader.getController();
            // Pasar las transacciones actuales al controlador de reportes
            controller.setTransactions(transactions);

            Stage stage = new Stage();
            stage.setTitle("Transaction Reports");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
