package com.example.submate.presentation.signup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;
import com.example.submate.presentation.onboarding.activity.OnboardingActivity;

public class SignupCompletedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_completed);

        TextView tvWelcome = findViewById(R.id.tv_welcome);
        String nickname = getIntent().getStringExtra("nickname");
        if (nickname != null && !nickname.isEmpty()) {
            tvWelcome.setText(nickname + " 님, 반가워요!");
        }

        findViewById(R.id.btn_go_login).setOnClickListener(view -> {
            Intent intent = new Intent(this, OnboardingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}