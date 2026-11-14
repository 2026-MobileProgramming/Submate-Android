package com.example.submate.presentation.signup.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;

public class SignupProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_profile);

        findViewById(R.id.btn_signup_complete).setOnClickListener(view -> {
            startActivity(new Intent(this, SignupCompleteActivity.class));
            finish();
        });
    }
}
