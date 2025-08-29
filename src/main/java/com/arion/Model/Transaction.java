package com.arion.Model;

import java.time.LocalDate;
import javafx.beans.property.*;

public class Transaction {

    public enum TransactionType {
        INCOME, EXPENSE
    }

    // Properties para JavaFX binding
    private final StringProperty description;
    private final StringProperty category;
    private final ObjectProperty<LocalDate> date;
    private final DoubleProperty amount;
    private final ObjectProperty<TransactionType> type;
    private final StringProperty note;

    // Constructor principal con todas las propiedades
    public Transaction(String description, String category, LocalDate date, double amount, TransactionType type, String note) {
        this.description = new SimpleStringProperty(description);
        this.category = new SimpleStringProperty(category);
        this.date = new SimpleObjectProperty<>(date);
        this.amount = new SimpleDoubleProperty(amount);
        this.type = new SimpleObjectProperty<>(type);
        this.note = new SimpleStringProperty(note != null ? note : "");
    }

    // Constructor simplificado sin nota
    public Transaction(String description, String category, LocalDate date, double amount, TransactionType type) {
        this(description, category, date, amount, type, "");
    }

    // Constructor de compatibilidad para el DashboardViewController existente
    public Transaction(String category, String dateStr, double amount, boolean isIncome) {
        this.description = new SimpleStringProperty(category);
        this.category = new SimpleStringProperty(category);
        this.date = new SimpleObjectProperty<>(LocalDate.now()); // Usar fecha actual por defecto
        this.amount = new SimpleDoubleProperty(Math.abs(amount)); // Siempre positivo
        this.type = new SimpleObjectProperty<>(isIncome ? TransactionType.INCOME : TransactionType.EXPENSE);
        this.note = new SimpleStringProperty("");
    }

    // Getters simples
    public String getDescription() {
        return description.get();
    }

    public String getCategory() {
        return category.get();
    }

    public LocalDate getDate() {
        return date.get();
    }

    public double getAmount() {
        return amount.get();
    }

    public TransactionType getType() {
        return type.get();
    }

    public String getNote() {
        return note.get();
    }

    // Property getters para JavaFX binding
    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public ObjectProperty<TransactionType> typeProperty() {
        return type;
    }

    public StringProperty noteProperty() {
        return note;
    }

    // Setters
    public void setCategory(String category) {
        this.category.set(category);
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public void setType(TransactionType type) {
        this.type.set(type);
    }

    public void setNote(String note) {
        this.note.set(note != null ? note : "");
    }

    // Métodos de compatibilidad para el DashboardViewController
    public boolean isIncome() {
        return type.get() == TransactionType.INCOME;
    }

    // Método para obtener fecha como string formateado para compatibilidad
    public String getDateString() {
        LocalDate localDate = date.get();
        if (localDate != null) {
            // Convertir a string para compatibilidad
            if (localDate.equals(LocalDate.now())) {
                return "Today";
            } else if (localDate.equals(LocalDate.now().minusDays(1))) {
                return "Yesterday";
            } else {
                return localDate.toString();
            }
        }
        return "Unknown";
    }

    @Override
    public String toString() {
        return String.format("%s: %s $%.2f (%s)",
                getCategory(),
                getDescription(),
                getAmount(),
                getType().toString().toLowerCase());
    }
}
