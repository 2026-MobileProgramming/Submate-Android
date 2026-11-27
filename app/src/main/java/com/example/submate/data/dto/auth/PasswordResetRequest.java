package com.example.submate.data.dto.auth;

public class PasswordResetRequest {
    private final String email;
    private final String newPassword;
    private final String newPasswordConfirm;

    public PasswordResetRequest(String email, String newPassword, String newPasswordConfirm) {
        this.email = email;
        this.newPassword = newPassword;
        this.newPasswordConfirm = newPasswordConfirm;
    }
}