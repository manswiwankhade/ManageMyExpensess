package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText edtEmail;
    EditText edtPassword;
    Button btnLogin;
    TextView txtSignUp;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Connect Java with XML
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUp = findViewById(R.id.txtSignUp);

        // Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Password eye
        setupPasswordEye();

        // Login button
        btnLogin.setOnClickListener(v -> {

            String email =
                    edtEmail.getText().toString().trim();

            String password =
                    edtPassword.getText().toString();

            // Email empty
            if (email.isEmpty()) {

                edtEmail.setError(
                        "Please enter your email"
                );

                edtEmail.requestFocus();
                return;
            }

            // Email format
            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()) {

                edtEmail.setError(
                        "Enter a valid email"
                );

                edtEmail.requestFocus();
                return;
            }

            // Password empty
            if (password.isEmpty()) {

                edtPassword.setError(
                        "Please enter your password"
                );

                edtPassword.requestFocus();
                return;
            }

            // Disable button while logging in
            btnLogin.setEnabled(false);
            btnLogin.setText("Logging in...");

            // Firebase Email + Password authentication
            firebaseAuth
                    .signInWithEmailAndPassword(
                            email,
                            password
                    )
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent =
                                    new Intent(
                                            LoginActivity.this,
                                            MainActivity.class
                                    );

                            startActivity(intent);

                            finish();

                        } else {

                            btnLogin.setEnabled(true);
                            btnLogin.setText("Login");

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Invalid email or password",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        // Open Sign Up
        txtSignUp.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            SignupActivity.class
                    );

            startActivity(intent);
        });
    }

    // Eye button for password
    private void setupPasswordEye() {

        edtPassword.setOnTouchListener(
                (v, event) -> {

                    if (event.getAction()
                            == MotionEvent.ACTION_UP) {

                        int drawableWidth =
                                edtPassword
                                        .getCompoundDrawables()[2]
                                        .getBounds()
                                        .width();

                        if (event.getX()
                                >= edtPassword.getWidth()
                                - drawableWidth
                                - 50) {

                            int cursorPosition =
                                    edtPassword
                                            .getSelectionStart();

                            int inputType =
                                    edtPassword.getInputType();

                            if ((inputType
                                    & InputType
                                    .TYPE_TEXT_VARIATION_PASSWORD)
                                    == InputType
                                    .TYPE_TEXT_VARIATION_PASSWORD) {

                                // Show password
                                edtPassword.setInputType(
                                        InputType.TYPE_CLASS_TEXT
                                                | InputType
                                                .TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                                );

                            } else {

                                // Hide password
                                edtPassword.setInputType(
                                        InputType.TYPE_CLASS_TEXT
                                                | InputType
                                                .TYPE_TEXT_VARIATION_PASSWORD
                                );
                            }

                            edtPassword.setSelection(
                                    Math.max(
                                            0,
                                            cursorPosition
                                    )
                            );

                            return true;
                        }
                    }

                    return false;
                }
        );
    }
}