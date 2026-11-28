package com.example.submate.presentation.resetpassword.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;
import com.example.submate.domain.repository.AuthRepository;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.presentation.common.view.SubmateButton;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordEmailActivity extends AppCompatActivity {

    @Inject
    AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword_email);

        EditText etEmail = findViewById(R.id.et_email);
        SubmateButton btnSendCode = findViewById(R.id.btn_send_code);

        btnSendCode.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일 주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSendCode.setEnabled(false);

            authRepository.sendVerificationCode(email, "RESET_PW", new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    btnSendCode.setEnabled(true);
                    Toast.makeText(ResetPasswordEmailActivity.this, "인증 코드가 발송되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPasswordEmailActivity.this, ResetPasswordVerifyActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSendCode.setEnabled(true);
                    Toast.makeText(ResetPasswordEmailActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}