package com.example.submate.presentation.signup.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;

public class SignupEmailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_email);

        findViewById(R.id.btn_send_code).setOnClickListener(view -> {
            startActivity(new Intent(this, SignupVerifyActivity.class));
            finish();
        });
    }
}
