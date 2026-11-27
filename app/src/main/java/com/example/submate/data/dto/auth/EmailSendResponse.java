package com.example.submate.data.dto.auth;

public class EmailSendResponse {
    private String expiresAt;
    private String resendAvailableAt;

    public String getExpiresAt() { return expiresAt; }
    public String getResendAvailableAt() { return resendAvailableAt; }
}