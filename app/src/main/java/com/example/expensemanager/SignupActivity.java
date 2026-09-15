package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity {

    EditText edtName;
    EditText edtEmail;
    EditText edtPhone;
    EditText edtPassword;
    EditText edtConfirmPassword;
    Button btnSignup;

    FirebaseAuth firebaseAuth;

    String name;
    String email;
    String phone;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);

        firebaseAuth = FirebaseAuth.getInstance();

        setupPasswordEye(edtPassword);
        setupPasswordEye(edtConfirmPassword);

        btnSignup.setOnClickListener(v -> {

            name = edtName.getText().toString().trim();
            email = edtEmail.getText().toString().trim();
            phone = edtPhone.getText().toString().trim();
            password = edtPassword.getText().toString();

            String confirmPassword =
                    edtConfirmPassword.getText().toString();

            if (name.isEmpty()) {
                edtName.setError("Please enter your name");
                edtName.requestFocus();
                return;
            }

            if (email.isEmpty() ||
                    !android.util.Patterns.EMAIL_ADDRESS
                            .matcher(email)
                            .matches()) {

                edtEmail.setError("Enter a valid email");
                edtEmail.requestFocus();
                return;
            }

            if (phone.length() != 10 ||
                    !phone.matches("[6-9][0-9]{9}")) {

                edtPhone.setError(
                        "Enter a valid 10-digit mobile number"
                );
                edtPhone.requestFocus();
                return;
            }

            if (!isValidPassword(password)) {

                edtPassword.setError(
                        "Use 8+ characters with uppercase, lowercase and number"
                );
                edtPassword.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {

                edtConfirmPassword.setError(
                        "Passwords do not match"
                );
                edtConfirmPassword.requestFocus();
                return;
            }

            sendOTP();
        });
    }

    private boolean isValidPassword(String password) {

        if (password.length() < 8) {
            return false;
        }

        boolean upper = false;
        boolean lower = false;
        boolean number = false;

        for (char c : password.toCharArray()) {

            if (Character.isUpperCase(c)) {
                upper = true;
            }

            if (Character.isLowerCase(c)) {
                lower = true;
            }

            if (Character.isDigit(c)) {
                number = true;
            }
        }

        return upper && lower && number;
    }

    private void setupPasswordEye(EditText editText) {

        editText.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                int drawableWidth =
                        editText.getCompoundDrawables()[2]
                                .getBounds()
                                .width();

                if (event.getX() >=
                        editText.getWidth()
                                - drawableWidth
                                - 50) {

                    int position =
                            editText.getSelectionStart();

                    if ((editText.getInputType()
                            & InputType.TYPE_TEXT_VARIATION_PASSWORD)
                            == InputType.TYPE_TEXT_VARIATION_PASSWORD) {

                        editText.setInputType(
                                InputType.TYPE_CLASS_TEXT
                                        | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );

                    } else {

                        editText.setInputType(
                                InputType.TYPE_CLASS_TEXT
                                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );
                    }

                    editText.setSelection(
                            Math.max(0, position)
                    );

                    return true;
                }
            }

            return false;
        });
    }

    private void sendOTP() {

        btnSignup.setEnabled(false);
        btnSignup.setText("Sending OTP...");

        String fullPhoneNumber = "+91" + phone;

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(fullPhoneNumber)
                        .setTimeout(
                                60L,
                                TimeUnit.SECONDS
                        )
                        .setActivity(this)
                        .setCallbacks(
                                new PhoneAuthProvider
                                        .OnVerificationStateChangedCallbacks() {

                                    @Override
                                    public void onVerificationCompleted(
                                            com.google.firebase.auth
                                                    .PhoneAuthCredential credential) {
                                    }

                                    @Override
                                    public void onVerificationFailed(
                                            com.google.firebase.FirebaseException e) {

                                        btnSignup.setEnabled(true);
                                        btnSignup.setText(
                                                "Create Account"
                                        );

                                        android.widget.Toast.makeText(
                                                SignupActivity.this,
                                                "OTP failed: "
                                                        + e.getMessage(),
                                                android.widget.Toast.LENGTH_LONG
                                        ).show();
                                    }

                                    @Override
                                    public void onCodeSent(
                                            String verificationId,
                                            PhoneAuthProvider
                                                    .ForceResendingToken token) {

                                        btnSignup.setEnabled(true);
                                        btnSignup.setText(
                                                "Create Account"
                                        );

                                        Intent intent =
                                                new Intent(
                                                        SignupActivity.this,
                                                        OtpActivity.class
                                                );

                                        intent.putExtra(
                                                "verificationId",
                                                verificationId
                                        );

                                        intent.putExtra(
                                                "name",
                                                name
                                        );

                                        intent.putExtra(
                                                "email",
                                                email
                                        );

                                        intent.putExtra(
                                                "password",
                                                password
                                        );

                                        intent.putExtra(
                                                "phone",
                                                phone
                                        );

                                        startActivity(intent);
                                    }
                                }
                        )
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}