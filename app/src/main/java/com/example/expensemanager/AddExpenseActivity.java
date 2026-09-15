package com.example.expensemanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {

    EditText edtAmount;
    EditText edtDate;
    EditText edtNote;

    Spinner spinnerCategory;
    Spinner spinnerPayment;

    Button btnSaveExpense;
    TextView txtBack;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_expense);

        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        edtNote = findViewById(R.id.edtNote);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPayment = findViewById(R.id.spinnerPayment);

        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        txtBack = findViewById(R.id.txtBack);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupCategories();
        setupPaymentMethods();
        setupDatePicker();

        txtBack.setOnClickListener(v -> finish());

        btnSaveExpense.setOnClickListener(v -> saveExpense());
    }

    private void setupCategories() {

        String[] categories = {
                "Food & Dining",
                "Transport",
                "Shopping",
                "Education",
                "Bills & Utilities",
                "Entertainment",
                "Health",
                "Travel",
                "Technology",
                "Personal Care",
                "College Fees",
                "Other"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCategory.setAdapter(adapter);
    }

    private void setupPaymentMethods() {

        String[] paymentMethods = {
                "Cash",
                "UPI",
                "Debit Card",
                "Credit Card",
                "Net Banking",
                "Other"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        paymentMethods
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerPayment.setAdapter(adapter);
    }

    private void setupDatePicker() {

        edtDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog =
                    new DatePickerDialog(
                            AddExpenseActivity.this,
                            (view, year, month, day) -> {

                                Calendar selected =
                                        Calendar.getInstance();

                                selected.set(
                                        year,
                                        month,
                                        day
                                );

                                SimpleDateFormat format =
                                        new SimpleDateFormat(
                                                "dd MMM yyyy",
                                                Locale.getDefault()
                                        );

                                edtDate.setText(
                                        format.format(
                                                selected.getTime()
                                        )
                                );
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );

            dialog.show();
        });
    }

    private void saveExpense() {

        String amount =
                edtAmount.getText().toString().trim();

        String date =
                edtDate.getText().toString().trim();

        String note =
                edtNote.getText().toString().trim();

        String category =
                spinnerCategory.getSelectedItem().toString();

        String payment =
                spinnerPayment.getSelectedItem().toString();

        // Check amount
        if (amount.isEmpty()) {

            edtAmount.setError("Please enter an amount");
            edtAmount.requestFocus();
            return;
        }

        try {

            double value = Double.parseDouble(amount);

            if (value <= 0) {

                edtAmount.setError(
                        "Amount must be greater than 0"
                );

                edtAmount.requestFocus();
                return;
            }

        } catch (NumberFormatException e) {

            edtAmount.setError(
                    "Enter a valid amount"
            );

            edtAmount.requestFocus();
            return;
        }

        // Check date
        if (date.isEmpty()) {

            edtDate.setError(
                    "Please select a date"
            );

            edtDate.requestFocus();
            return;
        }

        // Check Firebase user
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

        // Create expense data
        Map<String, Object> expense =
                new HashMap<>();

        expense.put("amount", Double.parseDouble(amount));
        expense.put("category", category);
        expense.put("date", date);
        expense.put("paymentMethod", payment);
        expense.put("note", note);
        expense.put("type", "expense");
        expense.put(
                "createdAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp()
        );

        // Save to:
        // users → userId → expenses

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .add(expense)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            AddExpenseActivity.this,
                            "Expense saved successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AddExpenseActivity.this,
                            "Failed to save expense",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}