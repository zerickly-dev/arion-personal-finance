package com.arion.Controller;

import com.arion.Model.Transaction;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReportsViewController implements Initializable {

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> descriptionCol;
    @FXML private TableColumn<Transaction, String> categoryCol;
    @FXML private TableColumn<Transaction, LocalDate> dateCol;
    @FXML private TableColumn<Transaction, Double> amountCol;
    @FXML private TableColumn<Transaction, Void> actionsCol;
    @FXML private TextField filterField;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        setupFiltering();
    }

    // Método para recibir transacciones desde el Dashboard
    public void setTransactions(ObservableList<Transaction> transactions) {
        this.transactionList.setAll(transactions);
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
                        setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    } else {
                        setText(String.format("-$%,.2f", Math.abs(amount)));
                        setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
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
                            transactionList.remove(transaction);
                        }
                    });
                });
                editBtn.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    editTransaction(transaction);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private Button createIconButton(String svgContent, String styleClass) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.getStyleClass().add("icon");
        Button button = new Button();
        button.setGraphic(path);
        button.getStyleClass().addAll("button-icon", styleClass);
        button.setPrefSize(30, 30);
        return button;
    }

    private void setupFiltering() {
        FilteredList<Transaction> filteredData = new FilteredList<>(transactionList, b -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(transaction -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (transaction.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (transaction.getCategory().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Transaction> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(transactionsTable.comparatorProperty());
        transactionsTable.setItems(sortedData);
    }

    private void editTransaction(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/TransactionFormView.fxml"));
            Parent root = loader.load();

            TransactionFormController controller = loader.getController();
            controller.populateForm(transaction);

            Stage modalStage = new Stage();
            modalStage.setTitle("Edit Transaction");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(transactionsTable.getScene().getWindow());
            modalStage.setResizable(false);

            Scene scene = new Scene(root);
            modalStage.setScene(scene);

            modalStage.setOnHidden(e -> {
                // Refrescar la tabla para mostrar los cambios
                transactionsTable.refresh();
            });

            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void downloadTransactions() {
        // TODO: Implementar funcionalidad de descarga de transacciones
        System.out.println("Botón de descarga presionado - Funcionalidad pendiente de implementar");

        // Placeholder para mostrar que el botón funciona
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Descarga de Transacciones");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad de descarga será implementada próximamente.");
        alert.showAndWait();
    }
}
