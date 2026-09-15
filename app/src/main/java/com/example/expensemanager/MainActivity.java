package com.example.expensemanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // ==============================================
    // TEXT VIEWS
    // ==============================================

    TextView txtGreeting;
    TextView txtUserName;
    TextView txtBalance;
    TextView txtIncome;
    TextView txtExpense;
    TextView txtMonthSpent;
    TextView txtMonthPercentage;
    TextView txtSeeAll;
    TextView txtInsight;

    // ==============================================
    // NAVIGATION
    // ==============================================

    TextView navHome;
    TextView navAnalytics;
    TextView navProfile;

    // ==============================================
    // BUTTONS
    // ==============================================

    Button btnAddExpense;
    Button btnAddIncome;

    // ==============================================
    // CALCULATOR
    // ==============================================

    ImageView btnCalculator;

    // ==============================================
    // MONTH PROGRESS
    // ==============================================

    ProgressBar monthProgress;

    // ==============================================
    // RECYCLER VIEW
    // ==============================================

    RecyclerView recyclerRecentTransactions;

    ArrayList<TransactionModel> recentTransactions;
    TransactionAdapter transactionAdapter;

    // ==============================================
    // FIREBASE
    // ==============================================

    FirebaseAuth auth;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // ==============================================
        // CONNECT XML
        // ==============================================

        txtGreeting = findViewById(R.id.txtGreeting);
        txtUserName = findViewById(R.id.txtUserName);

        txtBalance = findViewById(R.id.txtBalance);
        txtIncome = findViewById(R.id.txtIncome);
        txtExpense = findViewById(R.id.txtExpense);

        txtMonthSpent = findViewById(R.id.txtMonthSpent);
        txtMonthPercentage = findViewById(R.id.txtMonthPercentage);

        txtSeeAll = findViewById(R.id.txtSeeAll);
        txtInsight = findViewById(R.id.txtInsight);

        navHome = findViewById(R.id.navHome);
        navAnalytics = findViewById(R.id.navAnalytics);
        navProfile = findViewById(R.id.navProfile);

        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnAddIncome = findViewById(R.id.btnAddIncome);

        // IMPORTANT:
        // btnCalculator is an ImageView in XML
        btnCalculator = findViewById(R.id.btnCalculator);

        monthProgress = findViewById(R.id.monthProgress);

        recyclerRecentTransactions =
                findViewById(R.id.recyclerRecentTransactions);


        // ==============================================
        // FIREBASE
        // ==============================================

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // ==============================================
        // USER NAME
        // ==============================================

        SharedPreferences preferences =
                getSharedPreferences(
                        "UserData",
                        MODE_PRIVATE
                );

        String name =
                preferences.getString(
                        "name",
                        "User"
                );

        txtUserName.setText(name);


        // ==============================================
        // REAL-TIME GREETING
        // ==============================================

        updateGreeting();


        // ==============================================
        // RECENT TRANSACTIONS
        // ==============================================

        recentTransactions =
                new ArrayList<>();

        recyclerRecentTransactions.setLayoutManager(
                new LinearLayoutManager(this)
        );

        transactionAdapter =
                new TransactionAdapter(
                        recentTransactions
                );

        recyclerRecentTransactions.setAdapter(
                transactionAdapter
        );


        // ==============================================
        // ADD EXPENSE
        // ==============================================

        btnAddExpense.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AddExpenseActivity.class
                    );

            startActivity(intent);
        });


        // ==============================================
        // ADD INCOME
        // ==============================================

        btnAddIncome.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            AddIncomeActivity.class
                    );

            startActivity(intent);
        });


        // ==============================================
        // CALCULATOR
        // ==============================================

        btnCalculator.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            CalculatorActivity.class
                    );

            startActivity(intent);
        });


        // ==============================================
        // PROFILE
        // ==============================================

        navProfile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            ProfileActivity.class
                    );

            startActivity(intent);
        });


        // ==============================================
        // ANALYTICS
        // ==============================================

        navAnalytics.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            DetailedAnalyticsActivity.class
                    );

            startActivity(intent);
        });


        // ==============================================
        // SEE ALL TRANSACTIONS
        // ==============================================

        txtSeeAll.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            MainActivity.this,
                            TransactionsActivity.class
                    );

            startActivity(intent);
        });
    }


    // ==================================================
    // ON RESUME
    // ==================================================

    @Override
    protected void onResume() {

        super.onResume();

        updateGreeting();

        loadFinancialData();

        loadMonthlySpending();

        loadRecentTransactions();
    }


    // ==================================================
    // REAL-TIME GREETING
    // ==================================================

    private void updateGreeting() {

        Calendar calendar =
                Calendar.getInstance();

        int hour =
                calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hour >= 5 && hour < 12) {

            greeting = "Good morning 👋";

        } else if (hour >= 12 && hour < 17) {

            greeting = "Good afternoon 👋";

        } else if (hour >= 17 && hour < 21) {

            greeting = "Good evening 👋";

        } else {

            greeting = "Good night 👋";
        }

        txtGreeting.setText(greeting);
    }


    // ==================================================
    // LOAD BALANCE / INCOME / EXPENSE
    // ==================================================

    private void loadFinancialData() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    double totalIncome = 0;
                    double totalExpense = 0;

                    for (QueryDocumentSnapshot document :
                            queryDocumentSnapshots) {

                        Double amount =
                                document.getDouble("amount");

                        String type =
                                document.getString("type");

                        if (amount == null) {
                            continue;
                        }

                        if ("income".equalsIgnoreCase(type)) {

                            totalIncome += amount;

                        } else if ("expense".equalsIgnoreCase(type)) {

                            totalExpense += amount;
                        }
                    }

                    double balance =
                            totalIncome - totalExpense;


                    txtIncome.setText(
                            "₹ " +
                                    formatAmount(totalIncome)
                    );

                    txtExpense.setText(
                            "₹ " +
                                    formatAmount(totalExpense)
                    );

                    txtBalance.setText(
                            "₹ " +
                                    formatAmount(balance)
                    );

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MainActivity.this,
                            "Unable to load balance",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ==================================================
    // LOAD THIS MONTH'S EXPENSE
    // ==================================================

    private void loadMonthlySpending() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        Calendar calendar =
                Calendar.getInstance();

        int currentMonth =
                calendar.get(Calendar.MONTH);

        int currentYear =
                calendar.get(Calendar.YEAR);

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    double monthlyExpense = 0;

                    for (QueryDocumentSnapshot document :
                            queryDocumentSnapshots) {

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
                                dateString.isEmpty()) {
                            continue;
                        }

                        try {

                            Date transactionDate =
                                    parseDate(dateString);

                            if (transactionDate == null) {
                                continue;
                            }

                            Calendar transactionCalendar =
                                    Calendar.getInstance();

                            transactionCalendar.setTime(
                                    transactionDate
                            );

                            int transactionMonth =
                                    transactionCalendar.get(
                                            Calendar.MONTH
                                    );

                            int transactionYear =
                                    transactionCalendar.get(
                                            Calendar.YEAR
                                    );

                            if (transactionMonth == currentMonth &&
                                    transactionYear == currentYear) {

                                monthlyExpense += amount;
                            }

                        } catch (Exception ignored) {
                        }
                    }


                    // ==========================================
                    // SHOW MONTHLY SPENDING
                    // ==========================================

                    txtMonthSpent.setText(
                            "₹ " +
                                    formatAmount(monthlyExpense)
                                    +
                                    " spent"
                    );


                    // ==========================================
                    // MONTHLY PERCENTAGE
                    //
                    // Percentage is based on total income.
                    // ==========================================

                    calculateMonthlyPercentage(
                            monthlyExpense
                    );


                    // ==========================================
                    // SMART INSIGHT
                    // ==========================================

                    updateSmartInsight(
                            monthlyExpense
                    );

                })
                .addOnFailureListener(e -> {

                    txtMonthSpent.setText(
                            "₹ 0.00 spent"
                    );

                    txtMonthPercentage.setText(
                            "0%"
                    );

                    monthProgress.setProgress(0);
                });
    }


    // ==================================================
    // MONTHLY PERCENTAGE
    // ==================================================

    private void calculateMonthlyPercentage(
            double monthlyExpense
    ) {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    double totalIncome = 0;

                    for (QueryDocumentSnapshot document :
                            queryDocumentSnapshots) {

                        String type =
                                document.getString("type");

                        Double amount =
                                document.getDouble("amount");

                        if (amount == null) {
                            continue;
                        }

                        if ("income".equalsIgnoreCase(type)) {

                            totalIncome += amount;
                        }
                    }


                    int percentage = 0;

                    if (totalIncome > 0) {

                        percentage =
                                (int) Math.round(
                                        (monthlyExpense /
                                                totalIncome) * 100
                                );
                    }

                    // Keep progress between 0 and 100

                    int progress =
                            Math.min(
                                    Math.max(
                                            percentage,
                                            0
                                    ),
                                    100
                            );

                    txtMonthPercentage.setText(
                            percentage + "%"
                    );

                    monthProgress.setProgress(
                            progress
                    );

                });
    }


    // ==================================================
    // SMART INSIGHT
    // ==================================================

    private void updateSmartInsight(
            double monthlyExpense
    ) {

        if (monthlyExpense <= 0) {

            txtInsight.setText(
                    "No expenses recorded this month yet. " +
                            "Add your first expense to start " +
                            "tracking your spending."
            );

            return;
        }

        if (monthlyExpense < 1000) {

            txtInsight.setText(
                    "Your spending is currently low this month. " +
                            "Keep tracking your expenses to maintain " +
                            "good control over your money."
            );

        } else if (monthlyExpense < 5000) {

            txtInsight.setText(
                    "You have started spending regularly this month. " +
                            "Keeping an eye on small purchases can help " +
                            "you avoid unnecessary spending."
            );

        } else if (monthlyExpense < 10000) {

            txtInsight.setText(
                    "Your monthly spending is growing. " +
                            "Review your recent transactions and check " +
                            "which purchases can be reduced."
            );

        } else {

            txtInsight.setText(
                    "Your spending has crossed ₹10,000 this month. " +
                            "Consider reviewing your largest expenses " +
                            "and setting a spending limit."
            );
        }
    }


    // ==================================================
    // LOAD RECENT TRANSACTIONS
    // ==================================================

    private void loadRecentTransactions() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    recentTransactions.clear();

                    int count = 0;

                    for (QueryDocumentSnapshot document :
                            queryDocumentSnapshots) {

                        if (count >= 3) {
                            break;
                        }

                        Double amount =
                                document.getDouble("amount");

                        if (amount == null) {
                            continue;
                        }

                        String category =
                                document.getString("category");

                        String note =
                                document.getString("note");

                        String date =
                                document.getString("date");

                        String payment =
                                document.getString(
                                        "paymentMethod"
                                );

                        String type =
                                document.getString("type");


                        if (category == null ||
                                category.isEmpty()) {

                            category = "Other";
                        }

                        if (note == null ||
                                note.isEmpty()) {

                            note = "No note";
                        }

                        if (date == null) {

                            date = "";
                        }

                        if (payment == null) {

                            payment = "";
                        }

                        if (type == null) {

                            type = "expense";
                        }


                        TransactionModel transaction =
                                new TransactionModel(
                                        category,
                                        note,
                                        date,
                                        payment,
                                        type,
                                        amount
                                );

                        recentTransactions.add(
                                transaction
                        );

                        count++;
                    }

                    transactionAdapter
                            .notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MainActivity.this,
                            "Unable to load recent transactions",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }


    // ==================================================
    // DATE PARSER
    // ==================================================

    private Date parseDate(String dateString) {

        String[] formats = {

                "dd MMM yyyy",

                "dd/MM/yyyy",

                "dd-MM-yyyy",

                "yyyy-MM-dd",

                "dd MMMM yyyy",

                "MM/dd/yyyy"
        };


        for (String format : formats) {

            try {

                SimpleDateFormat sdf =
                        new SimpleDateFormat(
                                format,
                                Locale.getDefault()
                        );

                sdf.setLenient(false);

                return sdf.parse(dateString);

            } catch (Exception ignored) {
            }
        }

        return null;
    }


    // ==================================================
    // FORMAT MONEY
    // ==================================================

    private String formatAmount(
            double amount
    ) {

        return String.format(
                Locale.getDefault(),
                "%.2f",
                amount
        );
    }
}