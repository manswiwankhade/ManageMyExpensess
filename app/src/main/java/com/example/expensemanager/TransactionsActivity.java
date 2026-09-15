package com.example.expensemanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TransactionsActivity extends AppCompatActivity {

    TextView txtBackTransactions;

    Button btnAll;
    Button btnIncomeFilter;
    Button btnExpenseFilter;

    RecyclerView recyclerTransactions;

    FirebaseAuth auth;
    FirebaseFirestore db;

    ArrayList<TransactionModel> allTransactions;
    ArrayList<TransactionModel> incomeTransactions;
    ArrayList<TransactionModel> expenseTransactions;

    TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transactions);

        txtBackTransactions =
                findViewById(R.id.txtBackTransactions);

        btnAll =
                findViewById(R.id.btnAll);

        btnIncomeFilter =
                findViewById(R.id.btnIncomeFilter);

        btnExpenseFilter =
                findViewById(R.id.btnExpenseFilter);

        recyclerTransactions =
                findViewById(R.id.recyclerTransactions);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        allTransactions = new ArrayList<>();
        incomeTransactions = new ArrayList<>();
        expenseTransactions = new ArrayList<>();

        recyclerTransactions.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter =
                new TransactionAdapter(
                        allTransactions
                );

        recyclerTransactions.setAdapter(adapter);

        txtBackTransactions.setOnClickListener(
                v -> finish()
        );

        btnAll.setOnClickListener(
                v -> showAll()
        );

        btnIncomeFilter.setOnClickListener(
                v -> showIncome()
        );

        btnExpenseFilter.setOnClickListener(
                v -> showExpenses()
        );

        loadTransactions();
    }

    private void loadTransactions() {

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
                .addOnSuccessListener(querySnapshot -> {

                    allTransactions.clear();
                    incomeTransactions.clear();
                    expenseTransactions.clear();

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {

                        Double amount =
                                document.getDouble("amount");

                        if (amount == null) {
                            continue;
                        }

                        String category =
                                document.getString("category");

                        String date =
                                document.getString("date");

                        String payment =
                                document.getString(
                                        "paymentMethod"
                                );

                        String note =
                                document.getString("note");

                        String type =
                                document.getString("type");

                        if (category == null)
                            category = "Other";

                        if (date == null)
                            date = "";

                        if (payment == null)
                            payment = "";

                        if (note == null ||
                                note.isEmpty()) {

                            note = "No note";
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

                        allTransactions.add(
                                transaction
                        );

                        if (type.equals("income")) {

                            incomeTransactions.add(
                                    transaction
                            );

                        } else {

                            expenseTransactions.add(
                                    transaction
                            );
                        }
                    }

                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            TransactionsActivity.this,
                            "Could not load transactions",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void showAll() {

        adapter =
                new TransactionAdapter(
                        allTransactions
                );

        recyclerTransactions.setAdapter(
                adapter
        );
    }

    private void showIncome() {

        adapter =
                new TransactionAdapter(
                        incomeTransactions
                );

        recyclerTransactions.setAdapter(
                adapter
        );
    }

    private void showExpenses() {

        adapter =
                new TransactionAdapter(
                        expenseTransactions
                );

        recyclerTransactions.setAdapter(
                adapter
        );
    }
}