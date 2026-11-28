package com.example.submate.data.dto.auth;

public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public UserInfo getUser() { return user; }

    public static class UserInfo {
        private Long id;
        private String email;
        private String nickname;

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getNickname() { return nickname; }
    }
}