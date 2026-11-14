package com.example.submate.presentation.resetpassword.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;

public class ResetPasswordNewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword_new);

        findViewById(R.id.btn_resetpassword_complete).setOnClickListener(view -> {
            startActivity(new Intent(this, ResetPasswordCompleteActivity.class));
            finish();
        });
    }
}