package com.example.submate.presentation.resetpassword.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;
import com.example.submate.domain.repository.AuthRepository;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.presentation.common.view.SubmateButton;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordVerifyActivity extends AppCompatActivity {

    @Inject
    AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword_verify);

        String email = getIntent().getStringExtra("email");

        EditText etAuthCode = findViewById(R.id.et_auth_code);
        SubmateButton btnVerifyCode = findViewById(R.id.btn_verify_code);
        ProgressBar pbLoading = findViewById(R.id.pb_loading);

        btnVerifyCode.setOnClickListener(view -> {
            String code = etAuthCode.getText().toString().trim();
            if (code.length() != 6) {
                Toast.makeText(this, "6자리 인증 코드를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            pbLoading.setVisibility(View.VISIBLE);
            btnVerifyCode.setEnabled(false);

            authRepository.verifyCode(email, "RESET_PW", code, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    pbLoading.setVisibility(View.GONE);
                    btnVerifyCode.setEnabled(true);
                    Intent intent = new Intent(ResetPasswordVerifyActivity.this, ResetPasswordSetActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    pbLoading.setVisibility(View.GONE);
                    btnVerifyCode.setEnabled(true);
                    Toast.makeText(ResetPasswordVerifyActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}