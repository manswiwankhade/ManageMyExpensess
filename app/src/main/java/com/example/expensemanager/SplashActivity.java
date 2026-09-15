package com.example.expensemanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show splash layout immediately
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (auth.getCurrentUser() != null) {

                startActivity(new Intent(
                        SplashActivity.this,
                        MainActivity.class
                ));

            } else {

                startActivity(new Intent(
                        SplashActivity.this,
                        LoginActivity.class
                ));
            }

            finish();

        }, SPLASH_TIME);
    }
}