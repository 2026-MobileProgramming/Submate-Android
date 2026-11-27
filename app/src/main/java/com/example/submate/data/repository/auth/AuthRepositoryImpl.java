package com.example.submate.data.repository.auth;

import com.example.submate.data.dto.auth.LoginResponse;
import com.example.submate.data.network.TokenManager;
import com.example.submate.data.service.auth.AuthService;
import com.example.submate.domain.repository.AuthRepository;
import com.example.submate.domain.repository.ResultCallback;

import javax.inject.Inject;

public class AuthRepositoryImpl implements AuthRepository {
    private final AuthService authService;
    private final TokenManager tokenManager;

    @Inject
    public AuthRepositoryImpl(AuthService authService, TokenManager tokenManager) {
        this.authService = authService;
        this.tokenManager = tokenManager;
    }

    @Override
    public void sendVerificationCode(String email, String purpose, ResultCallback<Void> callback) {
        authService.sendVerificationCode(email, purpose, callback);
    }

    @Override
    public void verifyCode(String email, String purpose, String code, ResultCallback<Void> callback) {
        authService.verifyCode(email, purpose, code, callback);
    }

    @Override
    public void signup(String email, String password, String passwordConfirm, String nickname, ResultCallback<Long> callback) {
        authService.signup(email, password, passwordConfirm, nickname, callback);
    }

    @Override
    public void login(String email, String password, ResultCallback<LoginResponse> callback) {
        authService.login(email, password, new ResultCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse data) {
                if (data != null && data.getAccessToken() != null) {
                    tokenManager.saveAccessToken(data.getAccessToken());
                }
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void resetPassword(String email, String newPassword, String newPasswordConfirm, ResultCallback<Void> callback) {
        authService.resetPassword(email, newPassword, newPasswordConfirm, callback);
    }
}