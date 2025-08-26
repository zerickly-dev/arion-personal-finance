package com.arion.Controller;



import com.arion.Model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardViewController implements Initializable {

    @FXML
    private PieChart expensesPieChart;

    @FXML
    private ListView<Transaction> transactionsListView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupPieChart();
        setupTransactionList();
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
        ObservableList<Transaction> transactions = FXCollections.observableArrayList(
                new Transaction("Salary", "Yesterday", 3200.00, true),
                new Transaction("Groceries", "Today", -85.50, false)
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
                    Label dateLabel = new Label(item.getDate());
                    dateLabel.setFont(Font.font("Arial", 12));
                    descriptionBox.getChildren().addAll(categoryLabel, dateLabel);

                    HBox amountBox = new HBox();
                    amountBox.setAlignment(Pos.CENTER_RIGHT);
                    HBox.setHgrow(amountBox, javafx.scene.layout.Priority.ALWAYS);

                    String amountStr = String.format("%.2f", item.getAmount());
                    Label amountLabel = new Label((item.isIncome() ? "+" : "") + "$" + amountStr);
                    amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    amountLabel.setStyle(item.isIncome() ? "-fx-text-fill: #2E7D32;" : "-fx-text-fill: #C62828;");

                    amountBox.getChildren().add(amountLabel);
                    hbox.getChildren().addAll(descriptionBox, amountBox);
                    setGraphic(hbox);
                }
            }
        });
    }
}
