package com.arion.Model;

import com.arion.Config.Database;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;

public class Budget {

    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final StringProperty category;
    private final DoubleProperty limitAmount;
    private final ObjectProperty<YearMonth> periodYearMonth; // Para presupuestos mensuales
    private final BooleanProperty active;

    // Constructor para nuevo presupuesto
    public Budget(String category, double limitAmount, YearMonth periodYearMonth) {
        this.id = new SimpleIntegerProperty(0);
        this.userId = new SimpleIntegerProperty(0);
        this.category = new SimpleStringProperty(category);
        this.limitAmount = new SimpleDoubleProperty(limitAmount);
        this.periodYearMonth = new SimpleObjectProperty<>(periodYearMonth);
        this.active = new SimpleBooleanProperty(true);
    }

    // Constructor para presupuesto existente
    public Budget(int id, int userId, String category, double limitAmount, YearMonth periodYearMonth, boolean active) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
        this.category = new SimpleStringProperty(category);
        this.limitAmount = new SimpleDoubleProperty(limitAmount);
        this.periodYearMonth = new SimpleObjectProperty<>(periodYearMonth);
        this.active = new SimpleBooleanProperty(active);
    }

    // Getters y Setters con propiedades JavaFX
    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getUserId() {
        return userId.get();
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public String getCategory() {
        return category.get();
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public double getLimitAmount() {
        return limitAmount.get();
    }

    public DoubleProperty limitAmountProperty() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount.set(limitAmount);
    }

    public YearMonth getPeriodYearMonth() {
        return periodYearMonth.get();
    }

    public ObjectProperty<YearMonth> periodYearMonthProperty() {
        return periodYearMonth;
    }

    public void setPeriodYearMonth(YearMonth periodYearMonth) {
        this.periodYearMonth.set(periodYearMonth);
    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    // Método para guardar un nuevo presupuesto en la base de datos
    public boolean save(int userId) {
        String sql = "INSERT INTO budgets (user_id, category, limit_amount, period_year_month, active) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setString(2, this.getCategory());
            stmt.setDouble(3, this.getLimitAmount());
            stmt.setString(4, this.getPeriodYearMonth().toString()); // Almacenamos como YYYY-MM
            stmt.setBoolean(5, this.isActive());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.setId(generatedKeys.getInt(1));
                        this.setUserId(userId);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para actualizar un presupuesto existente
    public boolean update() {
        String sql = "UPDATE budgets SET category = ?, limit_amount = ?, period_year_month = ?, active = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.getCategory());
            stmt.setDouble(2, this.getLimitAmount());
            stmt.setString(3, this.getPeriodYearMonth().toString());
            stmt.setBoolean(4, this.isActive());
            stmt.setInt(5, this.getId());
            stmt.setInt(6, this.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para eliminar un presupuesto
    public boolean delete() {
        String sql = "DELETE FROM budgets WHERE id = ? AND user_id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, this.getId());
            stmt.setInt(2, this.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método estático para obtener todos los presupuestos activos de un usuario
    public static List<Budget> getAllActive(int userId) {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND active = true ORDER BY period_year_month DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                YearMonth yearMonth = YearMonth.parse(rs.getString("period_year_month"));
                Budget budget = new Budget(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("category"),
                    rs.getDouble("limit_amount"),
                    yearMonth,
                    rs.getBoolean("active")
                );
                budgets.add(budget);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return budgets;
    }

    // Método estático para obtener presupuestos del mes actual
    public static List<Budget> getCurrentMonthBudgets(int userId) {
        List<Budget> budgets = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.now();
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND period_year_month = ? AND active = true";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, currentYearMonth.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                YearMonth yearMonth = YearMonth.parse(rs.getString("period_year_month"));
                Budget budget = new Budget(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("category"),
                    rs.getDouble("limit_amount"),
                    yearMonth,
                    rs.getBoolean("active")
                );
                budgets.add(budget);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return budgets;
    }

    // Método para verificar si se ha excedido un presupuesto
    public static boolean isBudgetExceeded(int userId, String category, YearMonth yearMonth) {
        Budget budget = getBudgetForCategoryAndMonth(userId, category, yearMonth);
        if (budget == null) {
            return false; // No hay presupuesto definido para esta categoría
        }

        double spent = getSpentAmountForCategoryInMonth(userId, category, yearMonth);
        return spent > budget.getLimitAmount();
    }

    // Método para obtener el presupuesto específico para una categoría y mes
    public static Budget getBudgetForCategoryAndMonth(int userId, String category, YearMonth yearMonth) {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND category = ? AND period_year_month = ? AND active = true";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, category);
            stmt.setString(3, yearMonth.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                YearMonth ym = YearMonth.parse(rs.getString("period_year_month"));
                return new Budget(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("category"),
                    rs.getDouble("limit_amount"),
                    ym,
                    rs.getBoolean("active")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Método para obtener el gasto actual de una categoría en un mes específico
    public static double getSpentAmountForCategoryInMonth(int userId, String category, YearMonth yearMonth) {
        String sql = "SELECT SUM(amount) as total FROM transactions " +
                     "WHERE user_id = ? AND category = ? AND type = 'EXPENSE' " +
                     "AND date BETWEEN ? AND ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDate startDate = yearMonth.atDay(1); // Primer día del mes
            LocalDate endDate = yearMonth.atEndOfMonth(); // Último día del mes

            stmt.setInt(1, userId);
            stmt.setString(2, category);
            stmt.setDate(3, java.sql.Date.valueOf(startDate));
            stmt.setDate(4, java.sql.Date.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Método para obtener todas las categorías que han excedido su presupuesto en el mes actual
    public static List<Budget> getExceededBudgets(int userId) {
        List<Budget> exceededBudgets = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        List<Budget> currentBudgets = getCurrentMonthBudgets(userId);
        for (Budget budget : currentBudgets) {
            double spent = getSpentAmountForCategoryInMonth(userId, budget.getCategory(), currentMonth);
            if (spent > budget.getLimitAmount()) {
                exceededBudgets.add(budget);
            }
        }

        return exceededBudgets;
    }
}
