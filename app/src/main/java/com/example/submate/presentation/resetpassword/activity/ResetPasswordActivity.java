package com.example.submate.presentation.resetpassword.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;

public class ResetPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword);

        findViewById(R.id.btn_send_code).setOnClickListener(view -> {
            startActivity(new Intent(this, ResetPasswordCodeActivity.class));
            finish();
        });
    }
}