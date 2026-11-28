package com.example.submate.data.service.auth;

import com.example.submate.data.dto.auth.EmailSendRequest;
import com.example.submate.data.dto.auth.EmailSendResponse;
import com.example.submate.data.dto.auth.EmailVerifyRequest;
import com.example.submate.data.dto.auth.EmailVerifyResponse;
import com.example.submate.data.dto.auth.LoginRequest;
import com.example.submate.data.dto.auth.LoginResponse;
import com.example.submate.data.dto.auth.PasswordResetRequest;
import com.example.submate.data.dto.auth.SignupRequest;
import com.example.submate.data.dto.auth.SignupResponse;
import com.example.submate.data.network.AuthApi;
import com.example.submate.domain.repository.ResultCallback;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthServiceImpl implements AuthService {
    private static final String NETWORK_ERROR = "네트워크 연결을 확인해주세요.";

    private final AuthApi api;

    @Inject
    public AuthServiceImpl(AuthApi api) {
        this.api = api;
    }

    @Override
    public void sendVerificationCode(String email, String purpose, ResultCallback<Void> callback) {
        api.sendVerificationCode(new EmailSendRequest(email, purpose))
                .enqueue(new Callback<EmailSendResponse>() {
                    @Override
                    public void onResponse(Call<EmailSendResponse> call, Response<EmailSendResponse> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(parseErrorMessage(response.errorBody(), response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<EmailSendResponse> call, Throwable t) {
                        callback.onError(NETWORK_ERROR);
                    }
                });
    }

    @Override
    public void verifyCode(String email, String purpose, String code, ResultCallback<Void> callback) {
        api.verifyCode(new EmailVerifyRequest(email, purpose, code))
                .enqueue(new Callback<EmailVerifyResponse>() {
                    @Override
                    public void onResponse(Call<EmailVerifyResponse> call, Response<EmailVerifyResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isVerified()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(parseErrorMessage(response.errorBody(), response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<EmailVerifyResponse> call, Throwable t) {
                        callback.onError(NETWORK_ERROR);
                    }
                });
    }

    @Override
    public void signup(String email, String password, String passwordConfirm, String nickname, ResultCallback<Long> callback) {
        api.signup(new SignupRequest(email, password, passwordConfirm, nickname))
                .enqueue(new Callback<SignupResponse>() {
                    @Override
                    public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body().getUserId());
                        } else {
                            callback.onError(parseErrorMessage(response.errorBody(), response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<SignupResponse> call, Throwable t) {
                        callback.onError(NETWORK_ERROR);
                    }
                });
    }

    @Override
    public void login(String email, String password, ResultCallback<LoginResponse> callback) {
        api.login(new LoginRequest(email, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(parseErrorMessage(response.errorBody(), response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        callback.onError(NETWORK_ERROR);
                    }
                });
    }

    @Override
    public void resetPassword(String email, String newPassword, String newPasswordConfirm, ResultCallback<Void> callback) {
        api.resetPassword(new PasswordResetRequest(email, newPassword, newPasswordConfirm))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(parseErrorMessage(response.errorBody(), response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError(NETWORK_ERROR);
                    }
                });
    }

    private String parseErrorMessage(ResponseBody errorBody, int httpCode) {
        try {
            if (errorBody != null) {
                JsonObject json = JsonParser.parseString(errorBody.string()).getAsJsonObject();
                String code = json.has("error") ? json.get("error").getAsString()
                        : (json.has("code") ? json.get("code").getAsString() : "");

                switch (code) {
                    case "INVALID_EMAIL":
                        return "올바른 이메일 형식을 입력해주세요.";
                    case "DUPLICATE_EMAIL":
                        return "이미 가입된 이메일입니다.";
                    case "USER_NOT_FOUND":
                        return "가입되지 않은 이메일입니다.";
                    case "RESEND_TOO_EARLY":
                        return "잠시 후 다시 시도해주세요. (60초 제한)";
                    case "CODE_MISMATCH":
                        int left = json.has("remainingAttempts") ? json.get("remainingAttempts").getAsInt() : 0;
                        return "인증 코드가 일치하지 않습니다. (남은 기회: " + left + "회)";
                    case "CODE_EXPIRED":
                        return "인증 시간이 만료되었습니다. 코드를 다시 발송해주세요.";
                    case "TOO_MANY_ATTEMPTS":
                        return "입력 횟수를 초과했습니다. 코드를 다시 발송해주세요.";
                    case "CODE_NOT_FOUND":
                        return "유효한 인증 요청이 없습니다. 이메일 인증부터 진행해주세요.";
                    case "EMAIL_NOT_VERIFIED":
                        return "이메일 인증이 완료되지 않았습니다.";
                    case "PASSWORD_MISMATCH":
                        return "비밀번호가 일치하지 않습니다.";
                    case "INVALID_PASSWORD":
                    case "INVALID_INPUT":
                        return "비밀번호 형식을 확인해주세요. (영문, 숫자, 특수문자 포함 8~20자)";
                    case "INVALID_NICKNAME":
                        return "닉네임은 한글, 영문, 숫자 1~8자만 가능합니다.";
                    case "INVALID_CREDENTIALS":
                        return "이메일 또는 비밀번호가 올바르지 않습니다.";
                }
            }
        } catch (Exception ignored) {
        }
        return "요청에 실패했습니다. (" + httpCode + ")";
    }
}