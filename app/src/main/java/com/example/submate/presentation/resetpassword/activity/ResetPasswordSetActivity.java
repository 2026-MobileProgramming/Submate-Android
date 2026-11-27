package com.example.submate.presentation.resetpassword.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.submate.R;
import com.example.submate.domain.repository.AuthRepository;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.presentation.common.view.SubmateButton;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ResetPasswordSetActivity extends AppCompatActivity {

    private static final String PASSWORD_REGEX =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,20}$";

    @Inject
    AuthRepository authRepository;

    private EditText etPassword, etPasswordConfirm;
    private TextView tvPasswordStatus;
    private SubmateButton btnResetComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resetpassword_set);

        String email = getIntent().getStringExtra("email");

        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        tvPasswordStatus = findViewById(R.id.tv_password_status);
        btnResetComplete = findViewById(R.id.btn_resetpassword_complete);

        // 초기 비활성화 상태 설정
        btnResetComplete.setEnabled(false);
        btnResetComplete.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray300));

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputs();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etPassword.addTextChangedListener(watcher);
        etPasswordConfirm.addTextChangedListener(watcher);

        btnResetComplete.setOnClickListener(view -> {
            String newPassword = etPassword.getText().toString();
            String newPasswordConfirm = etPasswordConfirm.getText().toString();

            btnResetComplete.setEnabled(false);

            authRepository.resetPassword(email, newPassword, newPasswordConfirm, new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    startActivity(new Intent(ResetPasswordSetActivity.this, ResetPasswordCompletedActivity.class));
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnResetComplete.setEnabled(true);
                    Toast.makeText(ResetPasswordSetActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void validateInputs() {
        String pw = etPassword.getText().toString();
        String pwConfirm = etPasswordConfirm.getText().toString();

        boolean isValidFormat = pw.matches(PASSWORD_REGEX);
        boolean isPwMatch = !pw.isEmpty() && pw.equals(pwConfirm);

        if (!pw.isEmpty() && !isValidFormat) {
            tvPasswordStatus.setText("영문, 숫자, 특수문자를 포함한 8~20자로 입력해 주세요");
            tvPasswordStatus.setTextColor(ContextCompat.getColor(this, R.color.gray500));
        } else if (isPwMatch) {
            tvPasswordStatus.setText("비밀번호가 확인되었어요!");
            tvPasswordStatus.setTextColor(ContextCompat.getColor(this, R.color.pri700));
        } else {
            tvPasswordStatus.setText("입력하신 비밀번호가 일치하지 않아요");
            tvPasswordStatus.setTextColor(ContextCompat.getColor(this, R.color.gray500));
        }

        if (isValidFormat && isPwMatch) {
            btnResetComplete.setEnabled(true);
            btnResetComplete.setBackgroundTintList(null);
        } else {
            btnResetComplete.setEnabled(false);
            btnResetComplete.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray300));
        }
    }
}