package com.example.expensemanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DailyAnalyticsActivity extends AppCompatActivity {

    TextView txtBack;
    TextView txtDate;
    TextView txtTotalSpent;
    TextView txtTransactionCount;

    TextView txtFood;
    TextView txtTransport;
    TextView txtEducation;
    TextView txtShopping;
    TextView txtEntertainment;
    TextView txtHealth;
    TextView txtOther;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_daily_analytics);

        // -----------------------------
        // CONNECT XML
        // -----------------------------

        txtBack = findViewById(R.id.txtBack);
        txtDate = findViewById(R.id.txtDate);
        txtTotalSpent = findViewById(R.id.txtTotalSpent);
        txtTransactionCount =
                findViewById(R.id.txtTransactionCount);

        txtFood = findViewById(R.id.txtFood);
        txtTransport = findViewById(R.id.txtTransport);
        txtEducation = findViewById(R.id.txtEducation);
        txtShopping = findViewById(R.id.txtShopping);
        txtEntertainment = findViewById(R.id.txtEntertainment);
        txtHealth = findViewById(R.id.txtHealth);
        txtOther = findViewById(R.id.txtOther);

        // -----------------------------
        // FIREBASE
        // -----------------------------

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // -----------------------------
        // DATE
        // -----------------------------

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                );

        txtDate.setText(
                dateFormat.format(new Date())
        );

        // -----------------------------
        // BACK
        // -----------------------------

        txtBack.setOnClickListener(v -> finish());

        // -----------------------------
        // LOAD DATA
        // -----------------------------

        loadDailyExpenses();
    }

    private void loadDailyExpenses() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            double food = 0;
                            double transport = 0;
                            double education = 0;
                            double shopping = 0;
                            double entertainment = 0;
                            double health = 0;
                            double other = 0;

                            int transactionCount = 0;

                            String today =
                                    new SimpleDateFormat(
                                            "dd MMM yyyy",
                                            Locale.getDefault()
                                    ).format(new Date());

                            for (QueryDocumentSnapshot document :
                                    queryDocumentSnapshots) {

                                String type =
                                        document.getString("type");

                                // Only expenses
                                if (!"expense".equals(type)) {
                                    continue;
                                }

                                String date =
                                        document.getString("date");

                                if (date == null ||
                                        !date.equals(today)) {
                                    continue;
                                }

                                Double amount =
                                        document.getDouble("amount");

                                if (amount == null) {
                                    continue;
                                }

                                String category =
                                        document.getString(
                                                "category"
                                        );

                                if (category == null) {
                                    category = "Other";
                                }

                                category =
                                        category.trim()
                                                .toLowerCase();

                                switch (category) {

                                    case "food":
                                        food += amount;
                                        break;

                                    case "transport":
                                    case "transportation":
                                        transport += amount;
                                        break;

                                    case "education":
                                        education += amount;
                                        break;

                                    case "shopping":
                                        shopping += amount;
                                        break;

                                    case "entertainment":
                                        entertainment += amount;
                                        break;

                                    case "health":
                                    case "medical":
                                        health += amount;
                                        break;

                                    default:
                                        other += amount;
                                        break;
                                }

                                transactionCount++;
                            }

                            double total =
                                    food
                                            + transport
                                            + education
                                            + shopping
                                            + entertainment
                                            + health
                                            + other;

                            // -------------------------
                            // SHOW DATA
                            // -------------------------

                            txtTotalSpent.setText(
                                    "₹ " +
                                            formatAmount(total)
                            );

                            txtTransactionCount.setText(
                                    transactionCount +
                                            " transactions today"
                            );

                            txtFood.setText(
                                    "₹ " +
                                            formatAmount(food)
                            );

                            txtTransport.setText(
                                    "₹ " +
                                            formatAmount(transport)
                            );

                            txtEducation.setText(
                                    "₹ " +
                                            formatAmount(education)
                            );

                            txtShopping.setText(
                                    "₹ " +
                                            formatAmount(shopping)
                            );

                            txtEntertainment.setText(
                                    "₹ " +
                                            formatAmount(entertainment)
                            );

                            txtHealth.setText(
                                    "₹ " +
                                            formatAmount(health)
                            );

                            txtOther.setText(
                                    "₹ " +
                                            formatAmount(other)
                            );
                        }
                )
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            DailyAnalyticsActivity.this,
                            "Unable to load today's expenses",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private String formatAmount(double amount) {

        return String.format(
                Locale.getDefault(),
                "%.2f",
                amount
        );
    }
}