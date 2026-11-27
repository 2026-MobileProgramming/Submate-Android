package com.example.submate.data.service.auth;

import com.example.submate.data.dto.auth.LoginResponse;
import com.example.submate.domain.repository.ResultCallback;

public interface AuthService {
    void sendVerificationCode(String email, String purpose, ResultCallback<Void> callback);

    void verifyCode(String email, String purpose, String code, ResultCallback<Void> callback);

    void signup(String email, String password, String passwordConfirm, String nickname, ResultCallback<Long> callback);

    void login(String email, String password, ResultCallback<LoginResponse> callback);

    void resetPassword(String email, String newPassword, String newPasswordConfirm, ResultCallback<Void> callback);
}