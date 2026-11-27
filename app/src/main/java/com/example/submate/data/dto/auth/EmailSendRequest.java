package com.example.submate.data.dto.auth;

public class EmailSendRequest {
    private final String email;
    private final String purpose; // "SIGNUP" 또는 "RESET_PW"

    public EmailSendRequest(String email, String purpose) {
        this.email = email;
        this.purpose = purpose;
    }
}