package com.arion.Model;



public class Transaction {
    private String category;
    private String date;
    private double amount;
    private boolean isIncome;

    public Transaction(String category, String date, double amount, boolean isIncome) {
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.isIncome = isIncome;
    }

    // Getters
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public boolean isIncome() { return isIncome; }
}