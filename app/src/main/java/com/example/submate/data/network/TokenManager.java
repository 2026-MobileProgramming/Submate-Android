package com.example.submate.data.network;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/** JWT 액세스 토큰을 SharedPreferences에 저장 및 조회 */
@Singleton
public class TokenManager {
    private static final String PREF_NAME = "submate_auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private final SharedPreferences prefs;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAccessToken(String token) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void clearAccessToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply();
    }
}