package com.example.submate.presentation.splash.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;
import com.example.submate.presentation.onboarding.activity.OnboardingActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DURATION = 3000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}
