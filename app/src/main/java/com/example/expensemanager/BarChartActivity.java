package com.example.expensemanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BarChartActivity extends AppCompatActivity {

    private TextView txtBack;
    private TextView txtTotal;
    private TextView txtAverage;
    private TextView txtHighestMonth;
    private TextView txtLowestMonth;
    private TextView txtTrend;
    private TextView txtSmartInsight;

    private BarChartView barChart;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final String[] monthNames = {
            "Jan", "Feb", "Mar", "Apr",
            "May", "Jun", "Jul", "Aug",
            "Sep", "Oct", "Nov", "Dec"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bar_chart);

        // -------------------------------------------------
        // CONNECT XML
        // -------------------------------------------------

        txtBack = findViewById(R.id.txtBack);
        txtTotal = findViewById(R.id.txtTotal);
        txtAverage = findViewById(R.id.txtAverage);
        txtHighestMonth = findViewById(R.id.txtHighestMonth);
        txtLowestMonth = findViewById(R.id.txtLowestMonth);
        txtTrend = findViewById(R.id.txtTrend);
        txtSmartInsight = findViewById(R.id.txtSmartInsight);
        barChart = findViewById(R.id.barChart);

        // -------------------------------------------------
        // FIREBASE
        // -------------------------------------------------

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtBack.setOnClickListener(v -> finish());

        loadLastSixMonths();
    }

    // =====================================================
    // LOAD LAST SIX MONTHS
    // =====================================================

    private void loadLastSixMonths() {

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

                    Calendar now =
                            Calendar.getInstance();

                    /*
                     * sixMonthTotals[0] = oldest month
                     * sixMonthTotals[5] = current month
                     */
                    double[] sixMonthTotals =
                            new double[6];

                    String[] sixMonthLabels =
                            new String[6];

                    Calendar[] targetMonths =
                            new Calendar[6];

                    // -----------------------------------------
                    // CREATE LAST 6 MONTHS
                    // -----------------------------------------

                    for (int i = 0; i < 6; i++) {

                        Calendar month =
                                Calendar.getInstance();

                        month.set(
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                1
                        );

                        month.add(
                                Calendar.MONTH,
                                -5 + i
                        );

                        targetMonths[i] = month;

                        sixMonthLabels[i] =
                                monthNames[
                                        month.get(
                                                Calendar.MONTH
                                        )
                                        ];
                    }

                    // -----------------------------------------
                    // READ FIRESTORE
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

                        String dateString =
                                document.getString("date");

                        if (amount == null ||
                                dateString == null ||
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

                        // -------------------------------------
                        // MATCH TO ONE OF LAST 6 MONTHS
                        // -------------------------------------

                        for (int i = 0; i < 6; i++) {

                            Calendar target =
                                    targetMonths[i];

                            int targetMonth =
                                    target.get(
                                            Calendar.MONTH
                                    );

                            int targetYear =
                                    target.get(
                                            Calendar.YEAR
                                    );

                            if (expenseMonth ==
                                    targetMonth &&
                                    expenseYear ==
                                            targetYear) {

                                sixMonthTotals[i] +=
                                        amount;

                                break;
                            }
                        }
                    }

                    // -----------------------------------------
                    // TOTAL
                    // -----------------------------------------

                    double total = 0;

                    for (double value :
                            sixMonthTotals) {

                        total += value;
                    }

                    // -----------------------------------------
                    // AVERAGE
                    // -----------------------------------------

                    double average =
                            total / 6.0;

                    // -----------------------------------------
                    // HIGHEST / LOWEST
                    // -----------------------------------------

                    int highestIndex = 0;
                    int lowestIndex = 0;

                    for (int i = 1; i < 6; i++) {

                        if (sixMonthTotals[i] >
                                sixMonthTotals[highestIndex]) {

                            highestIndex = i;
                        }

                        if (sixMonthTotals[i] <
                                sixMonthTotals[lowestIndex]) {

                            lowestIndex = i;
                        }
                    }

                    // -----------------------------------------
                    // UPDATE SUMMARY
                    // -----------------------------------------

                    txtTotal.setText(
                            "₹ " +
                                    formatAmount(total)
                    );

                    txtAverage.setText(
                            "₹ " +
                                    formatAmount(average)
                    );

                    txtHighestMonth.setText(
                            sixMonthLabels[highestIndex]
                                    + "  •  ₹"
                                    + formatAmount(
                                    sixMonthTotals[
                                            highestIndex
                                            ]
                            )
                    );

                    txtLowestMonth.setText(
                            sixMonthLabels[lowestIndex]
                                    + "  •  ₹"
                                    + formatAmount(
                                    sixMonthTotals[
                                            lowestIndex
                                            ]
                            )
                    );

                    // -----------------------------------------
                    // TREND
                    // -----------------------------------------

                    calculateTrend(
                            sixMonthTotals,
                            sixMonthLabels
                    );

                    // -----------------------------------------
                    // SMART INSIGHT
                    // -----------------------------------------

                    createSmartInsight(
                            sixMonthTotals,
                            sixMonthLabels
                    );

                    // -----------------------------------------
                    // SEND DATA TO CHART
                    // -----------------------------------------

                    ArrayList<String> labels =
                            new ArrayList<>();

                    ArrayList<Double> values =
                            new ArrayList<>();

                    for (int i = 0; i < 6; i++) {

                        labels.add(
                                sixMonthLabels[i]
                        );

                        values.add(
                                sixMonthTotals[i]
                        );
                    }

                    barChart.setData(
                            labels,
                            values
                    );
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            BarChartActivity.this,
                            "Unable to load spending data",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // =====================================================
    // TREND
    // =====================================================

    private void calculateTrend(
            double[] values,
            String[] labels) {

        double previous =
                values[4];

        double current =
                values[5];

        if (previous == 0 &&
                current == 0) {

            txtTrend.setText(
                    "→ No spending change"
            );

            return;
        }

        if (previous == 0) {

            txtTrend.setText(
                    "↑ Spending started this month"
            );

            return;
        }

        double change =
                ((current - previous) /
                        previous) * 100;

        if (change > 0) {

            txtTrend.setText(
                    "↑ Spending increased by "
                            + String.format(
                            Locale.getDefault(),
                            "%.1f%%",
                            change
                    )
            );

        } else if (change < 0) {

            txtTrend.setText(
                    "↓ Spending decreased by "
                            + String.format(
                            Locale.getDefault(),
                            "%.1f%%",
                            Math.abs(change)
                    )
            );

        } else {

            txtTrend.setText(
                    "→ Spending stayed the same"
            );
        }
    }

    // =====================================================
    // SMART INSIGHT
    // =====================================================

    private void createSmartInsight(
            double[] values,
            String[] labels) {

        double total = 0;

        int highestIndex = 0;

        for (int i = 0; i < values.length; i++) {

            total += values[i];

            if (values[i] >
                    values[highestIndex]) {

                highestIndex = i;
            }
        }

        if (total <= 0) {

            txtSmartInsight.setText(
                    "No spending data is available " +
                            "for the last six months yet."
            );

            return;
        }

        double percentage =
                (values[highestIndex] / total)
                        * 100;

        String message;

        if (percentage >= 40) {

            message =
                    labels[highestIndex]
                            + " was your highest-spending month, " +
                            "accounting for "
                            + String.format(
                            Locale.getDefault(),
                            "%.1f%%",
                            percentage
                    )
                            + " of your spending over the last six months.";

        } else {

            message =
                    "Your spending is reasonably distributed " +
                            "across the last six months. " +
                            labels[highestIndex]
                            + " had the highest spending.";
        }

        txtSmartInsight.setText(message);
    }

    // =====================================================
    // DATE PARSER
    // =====================================================

    private Date parseDate(
            String dateString) {

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.getDefault()
                    );

            format.setLenient(false);

            return format.parse(
                    dateString
            );

        } catch (Exception e) {

            return null;
        }
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