package com.example.submate.data.network;

import com.example.submate.data.dto.auth.EmailSendRequest;
import com.example.submate.data.dto.auth.EmailSendResponse;
import com.example.submate.data.dto.auth.EmailVerifyRequest;
import com.example.submate.data.dto.auth.EmailVerifyResponse;
import com.example.submate.data.dto.auth.LoginRequest;
import com.example.submate.data.dto.auth.LoginResponse;
import com.example.submate.data.dto.auth.PasswordResetRequest;
import com.example.submate.data.dto.auth.SignupRequest;
import com.example.submate.data.dto.auth.SignupResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/email/send")
    Call<EmailSendResponse> sendVerificationCode(@Body EmailSendRequest request);

    @POST("auth/email/verify")
    Call<EmailVerifyResponse> verifyCode(@Body EmailVerifyRequest request);

    @POST("auth/signup")
    Call<SignupResponse> signup(@Body SignupRequest request);

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/password/reset")
    Call<Void> resetPassword(@Body PasswordResetRequest request);
}