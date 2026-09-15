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

public class AddIncomeActivity extends AppCompatActivity {

    EditText edtIncomeAmount;
    EditText edtIncomeDate;
    EditText edtIncomeNote;

    Spinner spinnerIncomeCategory;
    Spinner spinnerIncomePayment;

    Button btnSaveIncome;
    TextView txtBackIncome;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_income);

        edtIncomeAmount = findViewById(R.id.edtIncomeAmount);
        edtIncomeDate = findViewById(R.id.edtIncomeDate);
        edtIncomeNote = findViewById(R.id.edtIncomeNote);

        spinnerIncomeCategory =
                findViewById(R.id.spinnerIncomeCategory);

        spinnerIncomePayment =
                findViewById(R.id.spinnerIncomePayment);

        btnSaveIncome = findViewById(R.id.btnSaveIncome);
        txtBackIncome = findViewById(R.id.txtBackIncome);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupIncomeCategories();
        setupPaymentMethods();
        setupDatePicker();

        txtBackIncome.setOnClickListener(v -> finish());

        btnSaveIncome.setOnClickListener(v -> saveIncome());
    }

    private void setupIncomeCategories() {

        String[] categories = {
                "Salary",
                "Freelance",
                "Scholarship",
                "Allowance",
                "Business",
                "Gift",
                "Investment",
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

        spinnerIncomeCategory.setAdapter(adapter);
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

        spinnerIncomePayment.setAdapter(adapter);
    }

    private void setupDatePicker() {

        edtIncomeDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog =
                    new DatePickerDialog(
                            AddIncomeActivity.this,
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

                                edtIncomeDate.setText(
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

    private void saveIncome() {

        String amount =
                edtIncomeAmount.getText()
                        .toString()
                        .trim();

        String date =
                edtIncomeDate.getText()
                        .toString()
                        .trim();

        String note =
                edtIncomeNote.getText()
                        .toString()
                        .trim();

        String category =
                spinnerIncomeCategory
                        .getSelectedItem()
                        .toString();

        String payment =
                spinnerIncomePayment
                        .getSelectedItem()
                        .toString();

        // Validate amount
        if (amount.isEmpty()) {

            edtIncomeAmount.setError(
                    "Please enter an amount"
            );

            edtIncomeAmount.requestFocus();
            return;
        }

        try {

            double value =
                    Double.parseDouble(amount);

            if (value <= 0) {

                edtIncomeAmount.setError(
                        "Amount must be greater than 0"
                );

                edtIncomeAmount.requestFocus();
                return;
            }

        } catch (NumberFormatException e) {

            edtIncomeAmount.setError(
                    "Enter a valid amount"
            );

            edtIncomeAmount.requestFocus();
            return;
        }

        // Validate date
        if (date.isEmpty()) {

            edtIncomeDate.setError(
                    "Please select a date"
            );

            edtIncomeDate.requestFocus();
            return;
        }

        // Check Firebase login
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

        // Create income object
        Map<String, Object> income =
                new HashMap<>();

        income.put(
                "amount",
                Double.parseDouble(amount)
        );

        income.put(
                "category",
                category
        );

        income.put(
                "date",
                date
        );

        income.put(
                "paymentMethod",
                payment
        );

        income.put(
                "note",
                note
        );

        income.put(
                "type",
                "income"
        );

        income.put(
                "createdAt",
                com.google.firebase.firestore.FieldValue
                        .serverTimestamp()
        );

        // Save to Firestore
        db.collection("users")
                .document(userId)
                .collection("expenses")
                .add(income)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(
                            AddIncomeActivity.this,
                            "Income saved successfully!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AddIncomeActivity.this,
                            "Failed to save income",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
}