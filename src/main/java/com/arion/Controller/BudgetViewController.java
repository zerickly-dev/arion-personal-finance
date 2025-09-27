package com.arion.Controller;

import com.arion.Model.Budget;
import com.arion.Model.Transaction;
import com.arion.Config.SessionManager;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.geometry.Pos;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BudgetViewController implements Initializable {

    @FXML private TableView<Budget> budgetsTable;
    @FXML private TableColumn<Budget, String> categoryCol;
    @FXML private TableColumn<Budget, Double> limitAmountCol;
    @FXML private TableColumn<Budget, Double> spentAmountCol;
    @FXML private TableColumn<Budget, Double> remainingCol;
    @FXML private TableColumn<Budget, ProgressBar> progressCol;
    @FXML private TableColumn<Budget, Budget> actionsCol;
    @FXML private Button backButton;
    @FXML private Button addNewBudgetButton;
    @FXML private ComboBox<YearMonth> monthYearComboBox;
    @FXML private ListView<String> alertsListView;

    private ObservableList<Budget> budgetsList = FXCollections.observableArrayList();
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private YearMonth currentYearMonth = YearMonth.now();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar tabla de presupuestos
        setupTable();

        // Configurar selector de mes/año
        setupMonthYearSelector();

        // Configurar botones
        setupButtons();

        // Cargar datos
        loadBudgetsForMonth(currentYearMonth);

        // Mostrar alertas de presupuestos excedidos
        loadBudgetAlerts();
    }

    private void setupTable() {
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        limitAmountCol.setCellValueFactory(cellData -> cellData.getValue().limitAmountProperty().asObject());
        limitAmountCol.setCellFactory(col -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });

        spentAmountCol.setCellValueFactory(cellData -> {
            Budget budget = cellData.getValue();
            double spent = Budget.getSpentAmountForCategoryInMonth(
                SessionManager.getInstance().getCurrentUserId(),
                budget.getCategory(),
                budget.getPeriodYearMonth()
            );
            return new ReadOnlyObjectWrapper<>(spent);
        });
        spentAmountCol.setCellFactory(col -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double spent, boolean empty) {
                super.updateItem(spent, empty);
                if (empty || spent == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(spent));
                }
            }
        });

        remainingCol.setCellValueFactory(cellData -> {
            Budget budget = cellData.getValue();
            double spent = Budget.getSpentAmountForCategoryInMonth(
                SessionManager.getInstance().getCurrentUserId(),
                budget.getCategory(),
                budget.getPeriodYearMonth()
            );
            double remaining = budget.getLimitAmount() - spent;
            return new ReadOnlyObjectWrapper<>(remaining);
        });
        remainingCol.setCellFactory(col -> new TableCell<Budget, Double>() {
            @Override
            protected void updateItem(Double remaining, boolean empty) {
                super.updateItem(remaining, empty);
                if (empty || remaining == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(remaining));
                    if (remaining < 0) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });

        progressCol.setCellValueFactory(cellData -> {
            Budget budget = cellData.getValue();
            double spent = Budget.getSpentAmountForCategoryInMonth(
                SessionManager.getInstance().getCurrentUserId(),
                budget.getCategory(),
                budget.getPeriodYearMonth()
            );
            double percentage = spent / budget.getLimitAmount();

            ProgressBar progressBar = new ProgressBar(percentage);
            progressBar.setPrefWidth(140);

            if (percentage >= 1.0) {
                progressBar.setStyle("-fx-accent: red;");
            } else if (percentage >= 0.8) {
                progressBar.setStyle("-fx-accent: orange;");
            } else {
                progressBar.setStyle("-fx-accent: green;");
            }

            return new ReadOnlyObjectWrapper<>(progressBar);
        });
        progressCol.setCellFactory(col -> new TableCell<Budget, ProgressBar>() {
            @Override
            protected void updateItem(ProgressBar progressBar, boolean empty) {
                super.updateItem(progressBar, empty);
                if (empty || progressBar == null) {
                    setGraphic(null);
                } else {
                    setGraphic(progressBar);
                }
            }
        });

        actionsCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        actionsCol.setCellFactory(col -> new TableCell<Budget, Budget>() {
            private final Button editButton = new Button("Editar");
            private final Button deleteButton = new Button("Eliminar");

            {
                editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    Budget budget = getTableView().getItems().get(getIndex());
                    showBudgetForm(budget);
                });

                deleteButton.setOnAction(event -> {
                    Budget budget = getTableView().getItems().get(getIndex());
                    confirmAndDeleteBudget(budget);
                });
            }

            @Override
            protected void updateItem(Budget budget, boolean empty) {
                super.updateItem(budget, empty);
                if (empty || budget == null) {
                    setGraphic(null);
                } else {
                    HBox container = new HBox(5);
                    container.setAlignment(Pos.CENTER);
                    container.getChildren().addAll(editButton, deleteButton);
                    setGraphic(container);
                }
            }
        });

        budgetsTable.setItems(budgetsList);
    }

    private void setupMonthYearSelector() {
        // Llenar con 12 meses, desde el actual hacia atrás
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            months.add(current.minusMonths(i));
        }

        monthYearComboBox.setItems(FXCollections.observableArrayList(months));
        monthYearComboBox.setValue(current);

        monthYearComboBox.setConverter(new StringConverter<YearMonth>() {
            @Override
            public String toString(YearMonth yearMonth) {
                if (yearMonth != null) {
                    return yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                }
                return "";
            }

            @Override
            public YearMonth fromString(String string) {
                return null; // No necesario para este caso
            }
        });

        monthYearComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadBudgetsForMonth(newValue);
            }
        });
    }

    private void setupButtons() {
        backButton.setOnAction(event -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Fxml/DashboardView.fxml"));
                Scene scene = new Scene(root);
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        addNewBudgetButton.setOnAction(event -> showBudgetForm(null));
    }

    private void loadBudgetsForMonth(YearMonth yearMonth) {
        budgetsList.clear();
        int userId = SessionManager.getInstance().getCurrentUserId();

        // Obtenemos todos los presupuestos para el mes seleccionado
        for (Budget budget : Budget.getAllActive(userId)) {
            if (budget.getPeriodYearMonth().equals(yearMonth)) {
                budgetsList.add(budget);
            }
        }
    }

    private void loadBudgetAlerts() {
        alertsListView.getItems().clear();
        List<Budget> exceededBudgets = Budget.getExceededBudgets(SessionManager.getInstance().getCurrentUserId());

        if (exceededBudgets.isEmpty()) {
            alertsListView.getItems().add("No hay presupuestos excedidos en el mes actual.");
        } else {
            for (Budget budget : exceededBudgets) {
                double spent = Budget.getSpentAmountForCategoryInMonth(
                    SessionManager.getInstance().getCurrentUserId(),
                    budget.getCategory(),
                    budget.getPeriodYearMonth()
                );
                double exceeded = spent - budget.getLimitAmount();
                String alert = String.format(
                    "¡ALERTA! Has excedido tu presupuesto en %s por %s (%.1f%% del límite)",
                    budget.getCategory(),
                    currencyFormat.format(exceeded),
                    (spent / budget.getLimitAmount() * 100)
                );
                alertsListView.getItems().add(alert);
            }
        }
    }

    private void showBudgetForm(Budget budget) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/BudgetFormView.fxml"));
            Parent root = loader.load();

            BudgetFormController controller = loader.getController();
            controller.setOnSaveCallback(() -> {
                loadBudgetsForMonth((YearMonth) monthYearComboBox.getValue());
                loadBudgetAlerts();
            });

            if (budget != null) {
                controller.setBudgetToEdit(budget);
            } else {
                controller.setupForNewBudget(currentYearMonth);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(budget == null ? "Nuevo Presupuesto" : "Editar Presupuesto");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al abrir el formulario de presupuesto", e.getMessage());
        }
    }

    private void confirmAndDeleteBudget(Budget budget) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Estás seguro que deseas eliminar este presupuesto?");
        alert.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (budget.delete()) {
                loadBudgetsForMonth((YearMonth) monthYearComboBox.getValue());
                loadBudgetAlerts();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el presupuesto", "Ocurrió un error al intentar eliminar el presupuesto.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
