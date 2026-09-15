package com.example.expensemanager;

public class TransactionModel {

    private String category;
    private String note;
    private String date;
    private String paymentMethod;
    private String type;
    private double amount;

    public TransactionModel(
            String category,
            String note,
            String date,
            String paymentMethod,
            String type,
            double amount) {

        this.category = category;
        this.note = note;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.type = type;
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public String getNote() {
        return note;
    }

    public String getDate() {
        return date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getAmountText() {

        if (type.equals("income")) {

            return "+ ₹" +
                    String.format(
                            java.util.Locale.getDefault(),
                            "%.2f",
                            amount
                    );

        } else {

            return "- ₹" +
                    String.format(
                            java.util.Locale.getDefault(),
                            "%.2f",
                            amount
                    );
        }
    }
}