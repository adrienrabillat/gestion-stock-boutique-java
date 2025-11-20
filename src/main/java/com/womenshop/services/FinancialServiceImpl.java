package com.womenshop.services;

import com.womenshop.utils.DatabaseConnection;
import java.sql.*;

public class FinancialServiceImpl implements FinancialService {

    private Connection connection;

    public FinancialServiceImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    private void updateFinancials(double incomeToAdd, double costToAdd) {
        String sql = "UPDATE financials SET global_income = global_income + ?, global_cost = global_cost + ? WHERE id = 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, incomeToAdd);
            stmt.setDouble(2, costToAdd);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getInitialCapital() {
        return 10000.0; // Valeur par défaut ou récupérée de la DB
    }

    @Override
    public double getGlobalIncome() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT global_income FROM financials WHERE id = 1")) {
            if (rs.next()) return rs.getDouble("global_income");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    @Override
    public double getGlobalCost() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT global_cost FROM financials WHERE id = 1")) {
            if (rs.next()) return rs.getDouble("global_cost");
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    @Override
    public double getCapital() {
        // Règle métier : capital = initial + income - cost
        double initial = 0;
        double income = 0;
        double cost = 0;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM financials WHERE id = 1")) {
            if (rs.next()) {
                initial = rs.getDouble("initial_capital");
                income = rs.getDouble("global_income");
                cost = rs.getDouble("global_cost");
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return initial + income - cost;
    }

    @Override
    public void addIncome(double amount) {
        updateFinancials(amount, 0);
    }

    @Override
    public void addCost(double amount) {
        updateFinancials(0, amount);
    }
}