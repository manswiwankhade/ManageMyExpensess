package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailedAnalyticsActivity extends AppCompatActivity {

    private TextView txtBackDetailed;
    private TextView txtTotalExpense;
    private TextView btnPieChart;
    private TextView btnBarChart;
    private TextView txtSmartInsight;

    private LinearLayout layoutCategoryDetails;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<String> categoryNames = new ArrayList<>();
    private final List<Double> categoryAmounts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detailed_analytics);

        // ---------------------------------------------
        // CONNECT XML
        // ---------------------------------------------

        txtBackDetailed =
                findViewById(R.id.txtBackDetailed);

        txtTotalExpense =
                findViewById(R.id.txtTotalExpense);

        layoutCategoryDetails =
                findViewById(R.id.layoutCategoryDetails);

        btnPieChart =
                findViewById(R.id.btnPieChart);

        btnBarChart =
                findViewById(R.id.btnBarChart);

        txtSmartInsight =
                findViewById(R.id.txtSmartInsight);

        // ---------------------------------------------
        // FIREBASE
        // ---------------------------------------------

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ---------------------------------------------
        // BACK
        // ---------------------------------------------

        txtBackDetailed.setOnClickListener(v -> finish());

        // ---------------------------------------------
        // PIE CHART
        // ---------------------------------------------

        btnPieChart.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DetailedAnalyticsActivity.this,
                            PieChartActivity.class
                    );

            startActivity(intent);
        });

        // ---------------------------------------------
        // BAR CHART
        // ---------------------------------------------

        btnBarChart.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            DetailedAnalyticsActivity.this,
                            BarChartActivity.class
                    );

            startActivity(intent);
        });

        // ---------------------------------------------
        // LOAD DATA
        // ---------------------------------------------

        loadAnalytics();
    }

    // =====================================================
    // LOAD CATEGORY DATA
    // =====================================================

    private void loadAnalytics() {

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
                .addOnSuccessListener(snapshot -> {

                    Map<String, Double> totals =
                            new LinkedHashMap<>();

                    double totalExpense = 0;

                    // -----------------------------------------
                    // READ EXPENSES
                    // -----------------------------------------

                    for (QueryDocumentSnapshot document :
                            snapshot) {

                        String type =
                                document.getString("type");

                        if (!"expense".equals(type)) {
                            continue;
                        }

                        Double amount =
                                document.getDouble("amount");

                        if (amount == null) {
                            continue;
                        }

                        String category =
                                document.getString("category");

                        if (category == null ||
                                category.trim().isEmpty()) {

                            category = "Other";
                        }

                        Double oldAmount =
                                totals.get(category);

                        if (oldAmount == null) {
                            oldAmount = 0.0;
                        }

                        totals.put(
                                category,
                                oldAmount + amount
                        );

                        totalExpense += amount;
                    }

                    // -----------------------------------------
                    // TOTAL
                    // -----------------------------------------

                    txtTotalExpense.setText(
                            "Total Spending   ₹" +
                                    formatAmount(
                                            totalExpense
                                    )
                    );

                    // -----------------------------------------
                    // STORE DATA FOR CHARTS
                    // -----------------------------------------

                    categoryNames.clear();
                    categoryAmounts.clear();

                    for (Map.Entry<String, Double> entry :
                            totals.entrySet()) {

                        categoryNames.add(
                                entry.getKey()
                        );

                        categoryAmounts.add(
                                entry.getValue()
                        );
                    }

                    // -----------------------------------------
                    // SHOW CATEGORY LIST
                    // -----------------------------------------

                    showCategoryDetails(totals);

                    // -----------------------------------------
                    // SMART INSIGHT
                    // -----------------------------------------

                    createSmartInsight(totals, totalExpense);

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            DetailedAnalyticsActivity.this,
                            "Unable to load analytics",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // =====================================================
    // CATEGORY DETAILS
    // =====================================================

    private void showCategoryDetails(
            Map<String, Double> totals) {

        layoutCategoryDetails.removeAllViews();

        if (totals.isEmpty()) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "No expenses recorded yet."
            );

            empty.setTextSize(14);
            empty.setTextColor(
                    0xFF8B8494
            );

            layoutCategoryDetails.addView(
                    empty
            );

            return;
        }

        for (Map.Entry<String, Double> entry :
                totals.entrySet()) {

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            row.setGravity(
                    android.view.Gravity.CENTER_VERTICAL
            );

            row.setPadding(
                    4,
                    12,
                    4,
                    12
            );

            // -----------------------------------------
            // CATEGORY
            // -----------------------------------------

            TextView category =
                    new TextView(this);

            category.setText(
                    entry.getKey()
            );

            category.setTextSize(15);
            category.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

            category.setTextColor(
                    0xFF514B70
            );

            LinearLayout.LayoutParams
                    categoryParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );

            row.addView(
                    category,
                    categoryParams
            );

            // -----------------------------------------
            // AMOUNT
            // -----------------------------------------

            TextView amount =
                    new TextView(this);

            amount.setText(
                    "₹" +
                            formatAmount(
                                    entry.getValue()
                            )
            );

            amount.setTextSize(15);

            amount.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

            amount.setTextColor(
                    0xFF625B83
            );

            row.addView(amount);

            layoutCategoryDetails.addView(
                    row
            );
        }
    }

    // =====================================================
    // SMART INSIGHT
    // =====================================================

    private void createSmartInsight(
            Map<String, Double> totals,
            double totalExpense) {

        if (totals.isEmpty() ||
                totalExpense <= 0) {

            txtSmartInsight.setText(
                    "Add some expenses to receive a smart spending insight."
            );

            return;
        }

        String highestCategory = "";
        double highestAmount = 0;

        for (Map.Entry<String, Double> entry :
                totals.entrySet()) {

            if (entry.getValue() > highestAmount) {

                highestAmount =
                        entry.getValue();

                highestCategory =
                        entry.getKey();
            }
        }

        double percentage =
                (highestAmount /
                        totalExpense) * 100;

        String insight =
                "Your highest spending category is "
                        + highestCategory
                        + " at ₹"
                        + formatAmount(
                        highestAmount
                )
                        + ". It makes up "
                        + String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        percentage
                )
                        + " of your total spending.";

        txtSmartInsight.setText(
                insight
        );
    }

    // =====================================================
    // FORMAT
    // =====================================================

    private String formatAmount(
            double amount) {

        return String.format(
                Locale.getDefault(),
                "%.2f",
                amount
        );
    }
}