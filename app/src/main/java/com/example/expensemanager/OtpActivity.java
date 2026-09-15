package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OtpActivity extends AppCompatActivity {

    EditText edtOtp;
    Button btnVerify;
    TextView txtResend;

    FirebaseAuth firebaseAuth;

    String verificationId;
    String name;
    String email;
    String password;
    String phone;

    PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_otp);

        edtOtp = findViewById(R.id.edtOtp);
        btnVerify = findViewById(R.id.btnVerify);
        txtResend = findViewById(R.id.txtResend);

        firebaseAuth = FirebaseAuth.getInstance();

        verificationId =
                getIntent().getStringExtra("verificationId");

        name =
                getIntent().getStringExtra("name");

        email =
                getIntent().getStringExtra("email");

        password =
                getIntent().getStringExtra("password");

        phone =
                getIntent().getStringExtra("phone");

        // Verify OTP
        btnVerify.setOnClickListener(v -> {

            String otp =
                    edtOtp.getText().toString().trim();

            if (otp.length() != 6) {
                edtOtp.setError("Enter the 6-digit OTP");
                edtOtp.requestFocus();
                return;
            }

            verifyOTP(otp);
        });

        // Resend OTP
        txtResend.setOnClickListener(v -> {

            Toast.makeText(
                    OtpActivity.this,
                    "Please request a new OTP from the signup screen.",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void verifyOTP(String otp) {

        btnVerify.setEnabled(false);
        btnVerify.setText("Verifying...");

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(
                        verificationId,
                        otp
                );

        firebaseAuth
                .signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        // Phone verified
                        createEmailPasswordAccount();

                    } else {

                        btnVerify.setEnabled(true);
                        btnVerify.setText("Verify OTP");

                        Toast.makeText(
                                OtpActivity.this,
                                "Invalid OTP. Please try again.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void createEmailPasswordAccount() {

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        // Save only non-sensitive information
                        getSharedPreferences(
                                "UserData",
                                MODE_PRIVATE
                        )
                                .edit()
                                .putString("name", name)
                                .putString("email", email)
                                .putString("phone", phone)
                                .apply();

                        Toast.makeText(
                                OtpActivity.this,
                                "Account created successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent =
                                new Intent(
                                        OtpActivity.this,
                                        MainActivity.class
                                );

                        startActivity(intent);
                        finish();

                    } else {

                        btnVerify.setEnabled(true);
                        btnVerify.setText("Verify OTP");

                        String error =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Account creation failed";

                        Toast.makeText(
                                OtpActivity.this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}