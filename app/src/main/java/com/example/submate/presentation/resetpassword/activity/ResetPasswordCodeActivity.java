package com.example.submate.presentation.resetpassword.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;

public class ResetPasswordCodeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword_code);

        findViewById(R.id.btn_verify_code).setOnClickListener(view -> {
            startActivity(new Intent(this, ResetPasswordNewActivity.class));
            finish();
        });
    }
}