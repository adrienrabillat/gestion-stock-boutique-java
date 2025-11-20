package com.womenshop.services;

public interface FinancialService {
    double getInitialCapital();
    double getGlobalIncome();
    double getGlobalCost();
    double getCapital(); // Calcule le capital actuel

    void addIncome(double amount);
    void addCost(double amount);
}