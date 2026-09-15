package com.example.expensemanager;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PieChartActivity extends AppCompatActivity {

    private TextView txtBack;
    private TextView txtTotal;
    private TextView txtAverage;
    private TextView txtHighestCategory;
    private TextView txtHighestPercentage;
    private TextView txtPieInsight;

    private LinearLayout layoutLegend;

    private PieChartView pieChart;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ListenerRegistration expenseListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pie_chart);

        txtBack =
                findViewById(R.id.txtBack);

        txtTotal =
                findViewById(R.id.txtTotal);

        txtAverage =
                findViewById(R.id.txtAverage);

        txtHighestCategory =
                findViewById(R.id.txtHighestCategory);

        txtHighestPercentage =
                findViewById(R.id.txtHighestPercentage);

        txtPieInsight =
                findViewById(R.id.txtPieInsight);

        layoutLegend =
                findViewById(R.id.layoutLegend);

        pieChart =
                findViewById(R.id.pieChart);

        auth =
                FirebaseAuth.getInstance();

        db =
                FirebaseFirestore.getInstance();

        txtBack.setOnClickListener(
                v -> finish()
        );

        startRealtimeListener();
    }

    // =========================================================
    // FIREBASE LISTENER
    // =========================================================

    private void startRealtimeListener() {

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

        expenseListener =
                db.collection("users")
                        .document(userId)
                        .collection("expenses")
                        .addSnapshotListener(
                                (snapshot, error) -> {

                                    if (error != null) {

                                        Toast.makeText(
                                                PieChartActivity.this,
                                                "Unable to load expenses",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        return;
                                    }

                                    if (snapshot == null) {
                                        return;
                                    }

                                    loadCurrentMonthData(
                                            snapshot
                                    );
                                }
                        );
    }

    // =========================================================
    // CURRENT MONTH
    // =========================================================

    private void loadCurrentMonthData(
            QuerySnapshot snapshot) {

        Map<String, Double> categoryTotals =
                new LinkedHashMap<>();

        double totalExpense = 0;

        Calendar current =
                Calendar.getInstance();

        int currentMonth =
                current.get(Calendar.MONTH);

        int currentYear =
                current.get(Calendar.YEAR);

        for (QueryDocumentSnapshot document :
                snapshot) {

            String type =
                    document.getString("type");

            if (!"expense".equalsIgnoreCase(type)) {
                continue;
            }

            Double amount =
                    document.getDouble("amount");

            if (amount == null) {
                continue;
            }

            String dateString =
                    document.getString("date");

            if (dateString == null ||
                    dateString.trim().isEmpty()) {

                continue;
            }

            Date expenseDate =
                    parseDate(dateString);

            if (expenseDate == null) {
                continue;
            }

            Calendar expenseCalendar =
                    Calendar.getInstance();

            expenseCalendar.setTime(
                    expenseDate
            );

            int expenseMonth =
                    expenseCalendar.get(
                            Calendar.MONTH
                    );

            int expenseYear =
                    expenseCalendar.get(
                            Calendar.YEAR
                    );

            if (expenseMonth != currentMonth ||
                    expenseYear != currentYear) {

                continue;
            }

            String category =
                    document.getString("category");

            if (category == null ||
                    category.trim().isEmpty()) {

                category = "Other";
            }

            category = category.trim();

            double previous =
                    categoryTotals.containsKey(category)
                            ? categoryTotals.get(category)
                            : 0;

            categoryTotals.put(
                    category,
                    previous + amount
            );

            totalExpense += amount;
        }

        updateScreen(
                categoryTotals,
                totalExpense
        );
    }

    // =========================================================
    // UPDATE SCREEN
    // =========================================================

    private void updateScreen(
            Map<String, Double> categoryTotals,
            double totalExpense) {

        txtTotal.setText(
                "₹" + formatAmount(totalExpense)
        );

        Calendar calendar =
                Calendar.getInstance();

        int today =
                calendar.get(Calendar.DAY_OF_MONTH);

        double dailyAverage =
                today > 0
                        ? totalExpense / today
                        : 0;

        txtAverage.setText(
                "₹" + formatAmount(dailyAverage)
        );

        // -----------------------------------------------------
        // NO DATA
        // -----------------------------------------------------

        if (categoryTotals.isEmpty()) {

            pieChart.setData(
                    new ArrayList<>(),
                    new ArrayList<>()
            );

            txtHighestCategory.setText(
                    "No expenses yet"
            );

            txtHighestPercentage.setText(
                    "0%"
            );

            txtPieInsight.setText(
                    "You haven't added any expenses " +
                            "for this month yet. Add an expense " +
                            "to start tracking where your money goes."
            );

            layoutLegend.removeAllViews();

            return;
        }

        // -----------------------------------------------------
        // HIGHEST
        // -----------------------------------------------------

        String highestCategory = "";

        double highestAmount = 0;

        for (Map.Entry<String, Double> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() > highestAmount) {

                highestAmount =
                        entry.getValue();

                highestCategory =
                        entry.getKey();
            }
        }

        double highestPercentage =
                totalExpense > 0
                        ? highestAmount /
                        totalExpense * 100
                        : 0;

        txtHighestCategory.setText(
                highestCategory
        );

        txtHighestPercentage.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        highestPercentage
                )
        );

        // -----------------------------------------------------
        // PIE DATA
        // -----------------------------------------------------

        List<String> categories =
                new ArrayList<>();

        List<Double> amounts =
                new ArrayList<>();

        for (Map.Entry<String, Double> entry :
                categoryTotals.entrySet()) {

            if (entry.getValue() <= 0) {
                continue;
            }

            categories.add(
                    entry.getKey()
            );

            amounts.add(
                    entry.getValue()
            );
        }

        pieChart.setData(
                categories,
                amounts
        );

        // -----------------------------------------------------
        // VISIBLE COLOR LEGEND
        // -----------------------------------------------------

        createLegend(
                categoryTotals,
                totalExpense
        );

        // -----------------------------------------------------
        // INSIGHT
        // -----------------------------------------------------

        createInsight(
                highestCategory,
                highestAmount,
                highestPercentage
        );
    }

    // =========================================================
    // CREATE COLOR LEGEND
    // =========================================================

    private void createLegend(
            Map<String, Double> categoryTotals,
            double totalExpense) {

        layoutLegend.removeAllViews();

        for (Map.Entry<String, Double> entry :
                categoryTotals.entrySet()) {

            String category =
                    entry.getKey();

            double amount =
                    entry.getValue();

            if (amount <= 0) {
                continue;
            }

            double percentage =
                    totalExpense > 0
                            ? amount /
                            totalExpense * 100
                            : 0;

            // ROW
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
                    9,
                    4,
                    9
            );

            // -------------------------------------------------
            // COLOR DOT
            // -------------------------------------------------

            View colorDot =
                    new View(this);

            int dotSize =
                    dpToPx(14);

            LinearLayout.LayoutParams dotParams =
                    new LinearLayout.LayoutParams(
                            dotSize,
                            dotSize
                    );

            GradientDrawable dotBackground =
                    new GradientDrawable();

            dotBackground.setShape(
                    GradientDrawable.OVAL
            );

            dotBackground.setColor(
                    pieChart.getCategoryColor(
                            category
                    )
            );

            colorDot.setBackground(
                    dotBackground
            );

            row.addView(
                    colorDot,
                    dotParams
            );

            // -------------------------------------------------
            // CATEGORY NAME
            // -------------------------------------------------

            TextView categoryText =
                    new TextView(this);

            categoryText.setText(
                    category
            );

            categoryText.setTextSize(14);

            categoryText.setTextColor(
                    0xFF514B70
            );

            categoryText.setTypeface(
                    android.graphics.Typeface.DEFAULT_BOLD
            );

            LinearLayout.LayoutParams categoryParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );

            categoryParams.setMargins(
                    dpToPx(10),
                    0,
                    dpToPx(5),
                    0
            );

            row.addView(
                    categoryText,
                    categoryParams
            );

            // -------------------------------------------------
            // AMOUNT + %
            // -------------------------------------------------

            TextView amountText =
                    new TextView(this);

            amountText.setText(
                    "₹" +
                            formatAmount(amount) +
                            "  (" +
                            String.format(
                                    Locale.getDefault(),
                                    "%.1f%%",
                                    percentage
                            ) +
                            ")"
            );

            amountText.setTextSize(13);

            amountText.setTextColor(
                    0xFF81788F
            );

            amountText.setGravity(
                    android.view.Gravity.END
            );

            row.addView(
                    amountText
            );

            layoutLegend.addView(
                    row
            );
        }
    }

    // =========================================================
    // INSIGHT
    // =========================================================

    private void createInsight(
            String highestCategory,
            double highestAmount,
            double highestPercentage) {

        String insight;

        if (highestPercentage >= 50) {

            insight =
                    highestCategory +
                            " takes " +
                            String.format(
                                    Locale.getDefault(),
                                    "%.1f%%",
                                    highestPercentage
                            ) +
                            " of your spending this month. " +
                            "This is currently your biggest " +
                            "expense area.";

        } else if (highestPercentage >= 30) {

            insight =
                    highestCategory +
                            " is your largest expense category " +
                            "this month, using " +
                            String.format(
                                    Locale.getDefault(),
                                    "%.1f%%",
                                    highestPercentage
                            ) +
                            " of your total spending.";

        } else {

            insight =
                    "Your spending is spread across several " +
                            "categories this month. " +
                            highestCategory +
                            " is currently the largest at ₹" +
                            formatAmount(highestAmount) +
                            ".";
        }

        txtPieInsight.setText(
                insight
        );
    }

    // =========================================================
    // DATE
    // =========================================================

    private Date parseDate(
            String dateString) {

        String[] formats = {
                "dd MMM yyyy",
                "dd/MM/yyyy",
                "dd-MM-yyyy",
                "yyyy-MM-dd"
        };

        for (String pattern : formats) {

            try {

                SimpleDateFormat format =
                        new SimpleDateFormat(
                                pattern,
                                Locale.getDefault()
                        );

                format.setLenient(false);

                Date date =
                        format.parse(dateString);

                if (date != null) {
                    return date;
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    // =========================================================
    // MONEY
    // =========================================================

    private String formatAmount(
            double amount) {

        return String.format(
                Locale.getDefault(),
                "%.2f",
                amount
        );
    }

    private int dpToPx(int dp) {

        return (int) (
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (expenseListener != null) {
            expenseListener.remove();
        }
    }
}