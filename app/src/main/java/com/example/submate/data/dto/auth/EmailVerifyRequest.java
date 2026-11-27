package com.example.submate.data.dto.auth;

public class EmailVerifyRequest {
    private final String email;
    private final String purpose;
    private final String code;

    public EmailVerifyRequest(String email, String purpose, String code) {
        this.email = email;
        this.purpose = purpose;
        this.code = code;
    }
}