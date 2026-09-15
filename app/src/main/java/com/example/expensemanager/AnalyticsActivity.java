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

public class AnalyticsActivity extends AppCompatActivity {

    // =========================================================
    // VIEWS
    // =========================================================

    TextView txtBackAnalytics;
    TextView txtTodayExpense;
    TextView txtWeekExpense;
    TextView txtMonthExpense;
    TextView txtTransactionCount;
    TextView txtAverageExpense;


    // =========================================================
    // FIREBASE
    // =========================================================

    FirebaseAuth auth;
    FirebaseFirestore db;


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_analytics);


        // =====================================================
        // CONNECT XML
        // =====================================================

        txtBackAnalytics =
                findViewById(R.id.txtBackAnalytics);

        txtTodayExpense =
                findViewById(R.id.txtTodayExpense);

        txtWeekExpense =
                findViewById(R.id.txtWeekExpense);

        txtMonthExpense =
                findViewById(R.id.txtMonthExpense);

        txtTransactionCount =
                findViewById(R.id.txtTransactionCount);

        txtAverageExpense =
                findViewById(R.id.txtAverageExpense);


        // =====================================================
        // FIREBASE
        // =====================================================

        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();


        // =====================================================
        // BACK
        // =====================================================

        txtBackAnalytics.setOnClickListener(v -> finish());


        // =====================================================
        // LOAD ANALYTICS
        // =====================================================

        loadAnalytics();
    }


    // =========================================================
    // LOAD ANALYTICS
    // =========================================================

    private void loadAnalytics() {

        // -----------------------------------------------------
        // CHECK LOGIN
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // FIRESTORE
        // -----------------------------------------------------

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(querySnapshot -> {


                    // =================================================
                    // TOTALS
                    // =================================================

                    double todayTotal = 0;

                    double weekTotal = 0;

                    double monthTotal = 0;


                    int monthTransactionCount = 0;


                    // =================================================
                    // CURRENT DATE
                    // =================================================

                    Calendar now =
                            Calendar.getInstance();


                    int todayYear =
                            now.get(Calendar.YEAR);

                    int todayDay =
                            now.get(Calendar.DAY_OF_YEAR);


                    int currentMonth =
                            now.get(Calendar.MONTH);


                    int currentYear =
                            now.get(Calendar.YEAR);


                    // =================================================
                    // FIND START OF CURRENT WEEK
                    // =================================================

                    Calendar startOfWeek =
                            (Calendar) now.clone();

                    startOfWeek.set(
                            Calendar.DAY_OF_WEEK,
                            startOfWeek.getFirstDayOfWeek()
                    );

                    setStartOfDay(startOfWeek);


                    // =================================================
                    // FIND START OF CURRENT MONTH
                    // =================================================

                    Calendar startOfMonth =
                            (Calendar) now.clone();

                    startOfMonth.set(
                            Calendar.DAY_OF_MONTH,
                            1
                    );

                    setStartOfDay(startOfMonth);


                    // =================================================
                    // READ EVERY EXPENSE
                    // =================================================

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {


                        // -------------------------------------------------
                        // CHECK TYPE
                        // -------------------------------------------------

                        String type =
                                document.getString("type");


                        if (!"expense".equals(type)) {
                            continue;
                        }


                        // -------------------------------------------------
                        // GET AMOUNT
                        // -------------------------------------------------

                        Double amount =
                                document.getDouble("amount");


                        // -------------------------------------------------
                        // GET DATE
                        // -------------------------------------------------

                        String dateString =
                                document.getString("date");


                        if (amount == null ||
                                dateString == null ||
                                dateString.isEmpty()) {

                            continue;
                        }


                        // -------------------------------------------------
                        // CONVERT DATE
                        // -------------------------------------------------

                        Date transactionDate =
                                parseDate(dateString);


                        if (transactionDate == null) {
                            continue;
                        }


                        Calendar transaction =
                                Calendar.getInstance();

                        transaction.setTime(
                                transactionDate
                        );


                        // =================================================
                        // TODAY
                        // =================================================

                        int transactionDay =
                                transaction.get(
                                        Calendar.DAY_OF_YEAR
                                );

                        int transactionYear =
                                transaction.get(
                                        Calendar.YEAR
                                );


                        if (transactionDay == todayDay &&
                                transactionYear == todayYear) {

                            todayTotal += amount;
                        }


                        // =================================================
                        // THIS WEEK
                        // =================================================

                        if (!transaction.before(
                                startOfWeek
                        ) &&
                                !transaction.after(now)) {

                            weekTotal += amount;
                        }


                        // =================================================
                        // THIS MONTH
                        // =================================================

                        if (!transaction.before(
                                startOfMonth
                        ) &&
                                !transaction.after(now)) {

                            monthTotal += amount;

                            monthTransactionCount++;
                        }
                    }


                    // =====================================================
                    // DISPLAY TODAY
                    // =====================================================

                    txtTodayExpense.setText(
                            "₹ " +
                                    formatAmount(
                                            todayTotal
                                    )
                    );


                    // =====================================================
                    // DISPLAY WEEK
                    // =====================================================

                    txtWeekExpense.setText(
                            "₹ " +
                                    formatAmount(
                                            weekTotal
                                    )
                    );


                    // =====================================================
                    // DISPLAY MONTH
                    // =====================================================

                    txtMonthExpense.setText(
                            "₹ " +
                                    formatAmount(
                                            monthTotal
                                    )
                    );


                    // =====================================================
                    // TRANSACTION COUNT
                    // =====================================================

                    txtTransactionCount.setText(
                            "Transactions this month: " +
                                    monthTransactionCount
                    );


                    // =====================================================
                    // AVERAGE DAILY SPENDING
                    // =====================================================

                    int daysPassed =
                            now.get(
                                    Calendar.DAY_OF_MONTH
                            );


                    double average = 0;


                    if (daysPassed > 0) {

                        average =
                                monthTotal /
                                        daysPassed;
                    }


                    txtAverageExpense.setText(
                            "Average daily spending: ₹" +
                                    formatAmount(
                                            average
                                    )
                    );

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AnalyticsActivity.this,
                            "Unable to load analytics",
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }


    // =========================================================
    // PARSE DATE
    // =========================================================

    private Date parseDate(String dateString) {

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "dd MMM yyyy",
                            Locale.getDefault()
                    );

            format.setLenient(false);

            return format.parse(dateString);

        } catch (Exception e) {

            return null;
        }
    }


    // =========================================================
    // SET CALENDAR TO START OF DAY
    // =========================================================

    private void setStartOfDay(Calendar calendar) {

        calendar.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        calendar.set(
                Calendar.MINUTE,
                0
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );
    }


    // =========================================================
    // FORMAT AMOUNT
    // =========================================================

    private String formatAmount(double amount) {

        return String.format(
                Locale.getDefault(),
                "%.2f",
                amount
        );
    }
}