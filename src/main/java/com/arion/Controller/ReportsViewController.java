package com.arion.Controller;

import com.arion.Model.Transaction;
import com.arion.Config.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReportsViewController implements Initializable {

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> descriptionCol;
    @FXML private TableColumn<Transaction, String> categoryCol;
    @FXML private TableColumn<Transaction, LocalDate> dateCol;
    @FXML private TableColumn<Transaction, Double> amountCol;
    @FXML private TableColumn<Transaction, Void> actionsCol;
    @FXML private TextField filterField;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netBalanceLabel;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private FilteredList<Transaction> filteredTransactions;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserTransactions();
        setupTableColumns();
        setupFiltering();
        updateSummaryLabels();
    }

    private void loadUserTransactions() {
        int currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (currentUserId > 0) {
            List<Transaction> userTransactions = Transaction.getTransactionsByUser(currentUserId);
            transactionList.setAll(userTransactions);
        }
    }

    private void setupTableColumns() {
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Formato para la fecha
        dateCol.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Formato para el monto (con color)
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    if (transaction.getType() == Transaction.TransactionType.INCOME) {
                        setText(String.format("+$%,.2f", amount));
                        setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    } else {
                        setText(String.format("-$%,.2f", amount));
                        setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Celda para los botones de acción
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = createIconButton("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z", "button-icon-edit");
            private final Button deleteBtn = createIconButton("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z", "button-icon-delete");
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                pane.setAlignment(Pos.CENTER);
                deleteBtn.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "¿Estás seguro de que quieres eliminar esta transacción?",
                        ButtonType.YES, ButtonType.NO);
                    alert.setTitle("Confirmar eliminación");
                    alert.setHeaderText(null);

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            if (transaction.delete()) {
                                transactionList.remove(transaction);
                                updateSummaryLabels();
                                showAlert("Éxito", "Transacción eliminada correctamente", Alert.AlertType.INFORMATION);
                            } else {
                                showAlert("Error", "No se pudo eliminar la transacción");
                            }
                        }
                    });
                });

                editBtn.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    openEditTransactionForm(transaction);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupFiltering() {
        filteredTransactions = new FilteredList<>(transactionList, p -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredTransactions.setPredicate(transaction -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (transaction.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (transaction.getCategory().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                if (transaction.getNote().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        SortedList<Transaction> sortedTransactions = new SortedList<>(filteredTransactions);
        sortedTransactions.comparatorProperty().bind(transactionsTable.comparatorProperty());
        transactionsTable.setItems(sortedTransactions);
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

        // Cambiar color del balance neto
        if (netBalance >= 0) {
            netBalanceLabel.setStyle("-fx-text-fill: #4CAF50;");
        } else {
            netBalanceLabel.setStyle("-fx-text-fill: #F44336;");
        }
    }

    private Button createIconButton(String iconPath, String styleClass) {
        Button button = new Button();
        SVGPath icon = new SVGPath();
        icon.setContent(iconPath);
        icon.getStyleClass().add(styleClass);
        button.setGraphic(icon);
        button.getStyleClass().add("icon-button");
        return button;
    }

    private void openEditTransactionForm(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TransactionFormView.fxml"));
            Parent root = loader.load();

            TransactionFormController controller = loader.getController();
            controller.populateForm(transaction);
            controller.setOnTransactionSaved(() -> {
                loadUserTransactions();
                updateSummaryLabels();
            });

            Stage stage = new Stage();
            stage.setTitle("Editar Transacción");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error al cargar formulario de edición: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void addNewTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TransactionFormView.fxml"));
            Parent root = loader.load();

            TransactionFormController controller = loader.getController();
            controller.configureFor(TransactionFormController.FormType.EXPENSE);
            controller.setOnTransactionSaved(() -> {
                loadUserTransactions();
                updateSummaryLabels();
            });

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
    private void refreshData() {
        loadUserTransactions();
        updateSummaryLabels();
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
}
