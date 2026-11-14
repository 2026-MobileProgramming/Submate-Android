package com.example.submate.presentation.resetpassword.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;
import com.example.submate.presentation.onboarding.activity.OnboardingActivity;

public class ResetPasswordCompleteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword_complete);

        findViewById(R.id.btn_go_login).setOnClickListener(view -> {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
        });
    }
}