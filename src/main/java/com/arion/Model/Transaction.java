package com.arion.Model;

import com.arion.Config.Database;
import java.time.LocalDate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;

public class Transaction {

    public enum TransactionType {
        INCOME, EXPENSE
    }

    // Properties para JavaFX binding
    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final StringProperty description;
    private final StringProperty category;
    private final ObjectProperty<LocalDate> date;
    private final DoubleProperty amount;
    private final ObjectProperty<TransactionType> type;
    private final StringProperty note;

    // Constructor principal con todas las propiedades
    public Transaction(String description, String category, LocalDate date, double amount, TransactionType type, String note) {
        this.id = new SimpleIntegerProperty(0);
        this.userId = new SimpleIntegerProperty(0);
        this.description = new SimpleStringProperty(description);
        this.category = new SimpleStringProperty(category);
        this.date = new SimpleObjectProperty<>(date);
        this.amount = new SimpleDoubleProperty(amount);
        this.type = new SimpleObjectProperty<>(type);
        this.note = new SimpleStringProperty(note != null ? note : "");
    }

    // Constructor con ID (para transacciones existentes)
    public Transaction(int id, int userId, String description, String category, LocalDate date, double amount, TransactionType type, String note) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
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
        this.id = new SimpleIntegerProperty(0);
        this.userId = new SimpleIntegerProperty(0);
        this.description = new SimpleStringProperty(category);
        this.category = new SimpleStringProperty(category);
        this.date = new SimpleObjectProperty<>(LocalDate.now()); // Usar fecha actual por defecto
        this.amount = new SimpleDoubleProperty(Math.abs(amount)); // Siempre positivo
        this.type = new SimpleObjectProperty<>(isIncome ? TransactionType.INCOME : TransactionType.EXPENSE);
        this.note = new SimpleStringProperty("");
    }

    // Getters simples
    public int getId() {
        return id.get();
    }

    public int getUserId() {
        return userId.get();
    }

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
    public IntegerProperty idProperty() {
        return id;
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

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
    public void setId(int id) {
        this.id.set(id);
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

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

    // Método para guardar la transacción en la base de datos
    public boolean save() {
        if (getId() == 0) {
            return insert();
        } else {
            return update();
        }
    }

    // Método para insertar nueva transacción
    private boolean insert() {
        String query = "INSERT INTO transactions (user_id, description, category, date, amount, type, note) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, getUserId());
            stmt.setString(2, getDescription());
            stmt.setString(3, getCategory());
            stmt.setDate(4, Date.valueOf(getDate()));
            stmt.setDouble(5, getAmount());
            stmt.setString(6, getType().toString());
            stmt.setString(7, getNote());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error al insertar transacción: " + e.getMessage());
        }
        return false;
    }

    // Método para actualizar transacción existente
    private boolean update() {
        String query = "UPDATE transactions SET description = ?, category = ?, date = ?, amount = ?, type = ?, note = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, getDescription());
            stmt.setString(2, getCategory());
            stmt.setDate(3, Date.valueOf(getDate()));
            stmt.setDouble(4, getAmount());
            stmt.setString(5, getType().toString());
            stmt.setString(6, getNote());
            stmt.setInt(7, getId());
            stmt.setInt(8, getUserId());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar transacción: " + e.getMessage());
        }
        return false;
    }

    // Método para eliminar transacción
    public boolean delete() {
        if (getId() == 0) return false;

        String query = "DELETE FROM transactions WHERE id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, getId());
            stmt.setInt(2, getUserId());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al eliminar transacción: " + e.getMessage());
        }
        return false;
    }

    // Método estático para obtener todas las transacciones de un usuario
    public static List<Transaction> getTransactionsByUser(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT id, user_id, description, category, date, amount, type, note FROM transactions WHERE user_id = ? ORDER BY date DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getDate("date").toLocalDate(),
                    rs.getDouble("amount"),
                    TransactionType.valueOf(rs.getString("type")),
                    rs.getString("note")
                );
                transactions.add(transaction);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar transacciones: " + e.getMessage());
        }
        return transactions;
    }

    // Método estático para obtener transacciones recientes de un usuario
    public static List<Transaction> getRecentTransactionsByUser(int userId, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT id, user_id, description, category, date, amount, type, note FROM transactions WHERE user_id = ? ORDER BY date DESC, id DESC LIMIT ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getDate("date").toLocalDate(),
                    rs.getDouble("amount"),
                    TransactionType.valueOf(rs.getString("type")),
                    rs.getString("note")
                );
                transactions.add(transaction);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar transacciones recientes: " + e.getMessage());
        }
        return transactions;
    }

    // Método estático para obtener el balance total de un usuario
    public static double getTotalBalance(int userId) {
        String query = "SELECT SUM(CASE WHEN type = 'INCOME' THEN amount ELSE -amount END) as balance FROM transactions WHERE user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (Exception e) {
            System.err.println("Error al calcular balance: " + e.getMessage());
        }
        return 0.0;
    }

    // Método estático para obtener total de ingresos
    public static double getTotalIncome(int userId) {
        String query = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = ? AND type = 'INCOME'";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            System.err.println("Error al calcular ingresos totales: " + e.getMessage());
        }
        return 0.0;
    }

    // Método estático para obtener total de gastos
    public static double getTotalExpenses(int userId) {
        String query = "SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = ? AND type = 'EXPENSE'";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            System.err.println("Error al calcular gastos totales: " + e.getMessage());
        }
        return 0.0;
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
