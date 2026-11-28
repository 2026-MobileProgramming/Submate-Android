package com.example.submate.data.dto.auth;

public class SignupRequest {
    private final String email;
    private final String password;
    private final String passwordConfirm;
    private final String nickname;

    public SignupRequest(String email, String password, String passwordConfirm, String nickname) {
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.nickname = nickname;
    }
}