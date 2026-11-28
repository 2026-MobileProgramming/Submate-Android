package com.example.submate.presentation.onboarding.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.submate.R;
import com.example.submate.data.dto.auth.LoginResponse;
import com.example.submate.domain.repository.AuthRepository;
import com.example.submate.domain.repository.ResultCallback;
import com.example.submate.presentation.common.view.SubmateButton;
import com.example.submate.presentation.resetpassword.activity.ResetPasswordEmailActivity;
import com.example.submate.presentation.signup.activity.SignupEmailActivity;
import com.example.submate.presentation.subscribelist.activity.MainActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OnboardingActivity extends AppCompatActivity {

    @Inject
    AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding);

        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        SubmateButton btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);

            authRepository.login(email, password, new ResultCallback<LoginResponse>() {
                @Override
                public void onSuccess(LoginResponse data) {
                    btnLogin.setEnabled(true);
                    startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnLogin.setEnabled(true);
                    Toast.makeText(OnboardingActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        });

        findViewById(R.id.tv_find_password).setOnClickListener(view ->
                startActivity(new Intent(this, ResetPasswordEmailActivity.class))
        );

        findViewById(R.id.tv_signup).setOnClickListener(view ->
                startActivity(new Intent(this, SignupEmailActivity.class))
        );
    }
}