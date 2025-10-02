package com.arion.Controller;

import com.arion.Model.Transaction;
import com.arion.Model.Budget;
import com.arion.Config.SessionManager;
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
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

public class DashboardViewController implements Initializable {

    @FXML private PieChart expensesPieChart;
    @FXML private ListView<Transaction> transactionsListView;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netBalanceLabel;
    @FXML private Label usernameLabel;
    @FXML private Button budgetsButton;
    @FXML private TitledPane budgetAlertPane;
    @FXML private ListView<String> budgetAlertsListView;

    private ObservableList<Transaction> transactions;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserData();
        setupPieChart();
        setupTransactionList();
        updateSummaryLabels();
        setupBudgetAlerts();

        // Configurar el botón de presupuestos
        budgetsButton.setOnAction(event -> openBudgetManager());
    }

    private void loadUserData() {
        // Mostrar el nombre del usuario actual
        if (usernameLabel != null && SessionManager.getInstance().isLoggedIn()) {
            usernameLabel.setText("Bienvenido, " + SessionManager.getInstance().getCurrentUsername());
        }

        // Cargar transacciones del usuario actual
        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId > 0) {
            List<Transaction> userTransactions = Transaction.getRecentTransactionsByUser(currentUserId, 10);
            transactions = FXCollections.observableArrayList(userTransactions);
        } else {
            transactions = FXCollections.observableArrayList();
        }
    }

    private void setupPieChart() {
        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId <= 0) {
            expensesPieChart.setData(FXCollections.observableArrayList());
            return;
        }

        // Obtener todas las transacciones del usuario para el gráfico
        List<Transaction> allTransactions = Transaction.getTransactionsByUser(currentUserId);

        // Filtrar solo gastos y agrupar por categoría
        Map<String, Double> expensesByCategory = allTransactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                    Transaction::getCategory,
                    Collectors.summingDouble(Transaction::getAmount)
                ));

        // Crear datos para el gráfico de pastel
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        if (expensesByCategory.isEmpty()) {
            pieChartData.add(new PieChart.Data("Sin gastos", 1));
        } else {
            expensesByCategory.forEach((category, amount) ->
                pieChartData.add(new PieChart.Data(category, amount))
            );
        }

        expensesPieChart.setData(pieChartData);
        expensesPieChart.setTitle(null);
        expensesPieChart.setMinSize(PieChart.USE_PREF_SIZE, PieChart.USE_PREF_SIZE);
        expensesPieChart.setPrefSize(500, 400);
        expensesPieChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        expensesPieChart.setLabelsVisible(true);
    }

    private void setupTransactionList() {
        transactionsListView.setItems(transactions);

        // Configurar doble clic para editar transacciones
        transactionsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && transactionsListView.getSelectionModel().getSelectedItem() != null) {
                Transaction selectedTransaction = transactionsListView.getSelectionModel().getSelectedItem();
                openEditTransactionForm(selectedTransaction);
            }
        });

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

                    Label amountLabel = new Label(currencyFormat.format(item.getAmount()));
                    amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

                    // Color basado en el tipo de transacción
                    if (item.getType() == Transaction.TransactionType.INCOME) {
                        amountLabel.setStyle("-fx-text-fill: #4CAF50;"); // Verde para ingresos
                    } else {
                        amountLabel.setStyle("-fx-text-fill: #F44336;"); // Rojo para gastos
                    }

                    hbox.getChildren().addAll(descriptionBox, amountLabel);
                    HBox.setHgrow(descriptionBox, javafx.scene.layout.Priority.ALWAYS);

                    setGraphic(hbox);
                }
            }
        });
    }

    private void updateSummaryLabels() {
        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId <= 0) {
            totalIncomeLabel.setText("$0.00");
            totalExpensesLabel.setText("$0.00");
            netBalanceLabel.setText("$0.00");
            return;
        }

        double totalIncome = Transaction.getTotalIncome(currentUserId);
        double totalExpenses = Transaction.getTotalExpenses(currentUserId);
        double netBalance = totalIncome - totalExpenses;

        totalIncomeLabel.setText(currencyFormat.format(totalIncome));
        totalExpensesLabel.setText(currencyFormat.format(totalExpenses));
        netBalanceLabel.setText(currencyFormat.format(netBalance));

        // Cambiar color del balance neto según si es positivo o negativo
        if (netBalance >= 0) {
            netBalanceLabel.setStyle("-fx-text-fill: #4CAF50;"); // Verde para positivo
        } else {
            netBalanceLabel.setStyle("-fx-text-fill: #F44336;"); // Rojo para negativo
        }
    }

    private void setupBudgetAlerts() {
        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        YearMonth currentMonth = YearMonth.now();

        // Obtener presupuestos del usuario para el mes actual
        List<Budget> userBudgets = Budget.getCurrentMonthBudgets(currentUserId);

        // Configurar alertas de presupuesto
        if (userBudgets.isEmpty()) {
            budgetAlertPane.setVisible(false);
            budgetAlertPane.setManaged(false);
        } else {
            budgetAlertPane.setVisible(true);
            budgetAlertPane.setManaged(true);
            budgetAlertPane.setText("Alertas de Presupuesto");
            ObservableList<String> alerts = FXCollections.observableArrayList();

            for (Budget budget : userBudgets) {
                double totalExpenses = Transaction.getTotalExpensesByCategoryAndMonth(currentUserId, budget.getCategory(), currentMonth);
                if (totalExpenses > budget.getLimitAmount()) {
                    alerts.add("Te has excedido en " + budget.getCategory() + ": " + currencyFormat.format(totalExpenses - budget.getLimitAmount()) + " sobre el límite.");
                } else if (totalExpenses > budget.getLimitAmount() * 0.8) {
                    alerts.add("Estás cerca del límite en " + budget.getCategory() + ": " + currencyFormat.format(budget.getLimitAmount() - totalExpenses) + " restantes.");
                }
            }

            if (alerts.isEmpty()) {
                alerts.add("No tienes alertas de presupuesto para este mes.");
            }

            budgetAlertsListView.setItems(alerts);
        }
    }

    // Método para refrescar los datos (útil cuando se agrega una nueva transacción)
    public void refreshData() {
        loadUserData();
        setupPieChart();
        transactionsListView.setItems(transactions);
        updateSummaryLabels();
        setupBudgetAlerts();
    }

    @FXML
    private void openTransactionForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TransactionFormView.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del formulario
            TransactionFormController controller = loader.getController();

            // Configurar callback para refrescar datos cuando se guarde una transacción
            controller.setOnTransactionSaved(this::refreshData);

            Stage stage = new Stage();
            stage.setTitle("Nueva Transacción");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al cargar formulario de transacción: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void openReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/ReportsView.fxml"));
            Parent root = loader.load();

            // Obtener el controlador de reportes y pasarle una referencia de este dashboard
            ReportsViewController reportsController = loader.getController();
            reportsController.setDashboardRefreshCallback(this::refreshData);

            Stage stage = new Stage();
            stage.setTitle("Reportes");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, 800, 600));
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al cargar reportes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        SessionManager.getInstance().logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) totalIncomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void addIncome() {
        openTransactionFormWithType(TransactionFormController.FormType.INCOME);
    }

    @FXML
    private void addExpense() {
        openTransactionFormWithType(TransactionFormController.FormType.EXPENSE);
    }

    @FXML
    private void onViewReportsClick() {
        openReports();
    }

    private void openTransactionFormWithType(TransactionFormController.FormType formType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TransactionFormView.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del formulario
            TransactionFormController controller = loader.getController();

            // Configurar el tipo de formulario (Ingreso o Gasto)
            controller.configureFor(formType);

            // Configurar callback para refrescar datos cuando se guarde una transacción
            controller.setOnTransactionSaved(this::refreshData);

            Stage stage = new Stage();
            stage.setTitle(formType == TransactionFormController.FormType.INCOME ? "Agregar Ingreso" : "Agregar Gasto");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al cargar formulario de transacción: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openEditTransactionForm(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TransactionFormView.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del formulario
            TransactionFormController controller = loader.getController();

            // Configurar el formulario para edición
            controller.configureForEdit(transaction);

            // Configurar callback para refrescar datos cuando se guarde la transacción editada
            controller.setOnTransactionSaved(this::refreshData);

            Stage stage = new Stage();
            stage.setTitle("Editar Transacción");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al cargar formulario de edición de transacción: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para abrir el gestor de presupuestos
    private void openBudgetManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/BudgetView.fxml"));
            Parent root = loader.load();

            // Obtener el controlador para configurarlo si es necesario
            BudgetViewController controller = loader.getController();

            // Abrir en una nueva ventana
            Stage stage = new Stage();
            stage.setTitle("Gestión de Presupuestos");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root)); // Usar el tamaño definido en el FXML
            stage.showAndWait();

            // Refrescar datos cuando se cierre la ventana de presupuestos
            setupBudgetAlerts();

        } catch (IOException e) {
            System.err.println("Error al cargar gestión de presupuestos: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
