package com.example.submate.presentation.signup.activity;

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
public class SignupSetActivity extends AppCompatActivity {

    @Inject
    AuthRepository authRepository;

    private EditText etPassword, etPasswordConfirm, etNickname;
    private TextView tvPasswordStatus;
    private SubmateButton btnSignupComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_set);

        String email = getIntent().getStringExtra("email");

        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        tvPasswordStatus = findViewById(R.id.tv_password_status);
        etNickname = findViewById(R.id.et_nickname);
        btnSignupComplete = findViewById(R.id.btn_signup_complete);

        btnSignupComplete.setEnabled(false);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputs();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        etPassword.addTextChangedListener(watcher);
        etPasswordConfirm.addTextChangedListener(watcher);
        etNickname.addTextChangedListener(watcher);

        btnSignupComplete.setOnClickListener(view -> {
            String password = etPassword.getText().toString();
            String passwordConfirm = etPasswordConfirm.getText().toString();
            String nickname = etNickname.getText().toString().trim();

            btnSignupComplete.setEnabled(false);

            authRepository.signup(email, password, passwordConfirm, nickname, new ResultCallback<Long>() {
                @Override
                public void onSuccess(Long userId) {
                    Intent intent = new Intent(SignupSetActivity.this, SignupCompletedActivity.class);
                    intent.putExtra("nickname", nickname);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSignupComplete.setEnabled(true);
                    Toast.makeText(SignupSetActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void validateInputs() {
        String pw = etPassword.getText().toString();
        String pwConfirm = etPasswordConfirm.getText().toString();
        String nickname = etNickname.getText().toString().trim();

        boolean isValidLength = pw.length() >= 8 && pw.length() <= 20;
        boolean isPwMatch = !pw.isEmpty() && pw.equals(pwConfirm);
        boolean isValidNickname = nickname.matches("^[가-힣a-zA-Z0-9]{1,8}$");

        if (!pw.isEmpty() && !isValidLength) {
            tvPasswordStatus.setText("비밀번호는 8~20자로 입력해 주세요");
            tvPasswordStatus.setTextColor(ContextCompat.getColor(this, R.color.gray500));
        } else if (isPwMatch) {
            tvPasswordStatus.setText("비밀번호가 확인되었어요!");
            tvPasswordStatus.setTextColor(ContextCompat.getColor(this, R.color.pri700));
        } else {
            tvPasswordStatus.setText("입력하신 비밀번호가 일치하지 않아요");
            tvPasswordStatus.setTextColor(ContextCompat.getColor(this, R.color.gray500));
        }

        if (isValidLength && isPwMatch && isValidNickname) {
            btnSignupComplete.setEnabled(true);
            btnSignupComplete.setBackgroundTintList(null);
        } else {
            btnSignupComplete.setEnabled(false);
            btnSignupComplete.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray300));
        }
    }
}