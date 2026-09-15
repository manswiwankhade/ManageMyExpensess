package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryAnalyticsActivity extends AppCompatActivity {

    private TextView txtBack;
    private TextView txtTotalExpense;
    private TextView txtSmartInsight;
    private LinearLayout layoutCategories;

    private Button btnPieChart;
    private Button btnBarChart;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final List<String> categories = new ArrayList<>();
    private final List<Double> amounts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category_analytics);

        txtBack = findViewById(R.id.txtBack);
        txtTotalExpense = findViewById(R.id.txtTotalExpense);
        txtSmartInsight = findViewById(R.id.txtSmartInsight);
        layoutCategories = findViewById(R.id.layoutCategories);

        btnPieChart = findViewById(R.id.btnPieChart);
        btnBarChart = findViewById(R.id.btnBarChart);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtBack.setOnClickListener(v -> finish());

        btnPieChart.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CategoryAnalyticsActivity.this,
                    PieChartActivity.class
            );

            startActivity(intent);
        });

        btnBarChart.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CategoryAnalyticsActivity.this,
                    BarChartActivity.class
            );

            startActivity(intent);
        });

        loadCategoryData();
    }

    private void loadCategoryData() {

        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(snapshot -> {

                    categories.clear();
                    amounts.clear();

                    double total = 0;

                    for (QueryDocumentSnapshot document : snapshot) {

                        String type =
                                document.getString("type");

                        if (!"expense".equals(type)) {
                            continue;
                        }

                        Double amount =
                                document.getDouble("amount");

                        String category =
                                document.getString("category");

                        if (amount == null) {
                            continue;
                        }

                        if (category == null ||
                                category.trim().isEmpty()) {

                            category = "Other";
                        }

                        int index =
                                categories.indexOf(category);

                        if (index == -1) {

                            categories.add(category);
                            amounts.add(amount);

                        } else {

                            amounts.set(
                                    index,
                                    amounts.get(index) + amount
                            );
                        }

                        total += amount;
                    }

                    txtTotalExpense.setText(
                            "₹ " + formatAmount(total)
                    );

                    showCategories();

                    createSmartInsight();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            CategoryAnalyticsActivity.this,
                            "Unable to load analytics",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void showCategories() {

        layoutCategories.removeAllViews();

        if (categories.isEmpty()) {

            TextView empty = new TextView(this);

            empty.setText(
                    "No expense records available yet."
            );

            empty.setTextSize(16);
            empty.setTextColor(0xFF8B8494);
            empty.setPadding(10, 20, 10, 20);

            layoutCategories.addView(empty);

            return;
        }

        for (int i = 0; i < categories.size(); i++) {

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            row.setGravity(
                    android.view.Gravity.CENTER_VERTICAL
            );

            row.setPadding(
                    18,
                    18,
                    18,
                    18
            );

            row.setBackgroundColor(
                    0xFFFFFFFF
            );

            TextView category =
                    new TextView(this);

            category.setText(
                    categories.get(i)
            );

            category.setTextSize(15);
            category.setTextColor(0xFF514B70);
            category.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

            LinearLayout.LayoutParams categoryParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );

            row.addView(
                    category,
                    categoryParams
            );

            TextView amount =
                    new TextView(this);

            amount.setText(
                    "₹ " +
                            formatAmount(
                                    amounts.get(i)
                            )
            );

            amount.setTextSize(15);
            amount.setTextColor(0xFF625B83);
            amount.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

            row.addView(amount);

            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            rowParams.setMargins(
                    0,
                    0,
                    0,
                    8
            );

            layoutCategories.addView(
                    row,
                    rowParams
            );
        }
    }

    private void createSmartInsight() {

        if (categories.isEmpty()) {

            txtSmartInsight.setText(
                    "Add some expenses to get a smart spending insight."
            );

            return;
        }

        int highestIndex = 0;

        for (int i = 1; i < amounts.size(); i++) {

            if (amounts.get(i) >
                    amounts.get(highestIndex)) {

                highestIndex = i;
            }
        }

        double total = 0;

        for (double amount : amounts) {
            total += amount;
        }

        double highestAmount =
                amounts.get(highestIndex);

        double percentage =
                (highestAmount / total) * 100;

        String insight =
                "You spend the most on "
                        + categories.get(highestIndex)
                        + ". It accounts for "
                        + String.format(
                        Locale.getDefault(),
                        "%.1f",
                        percentage
                )
                        + "% of your total expenses.";

        txtSmartInsight.setText(insight);
    }

    private String formatAmount(double amount) {

        return String.format(
                Locale.getDefault(),
                "%.2f",
                amount
        );
    }
}