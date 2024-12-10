package com.example.golosovanie.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_NAME = "name";
    private static SessionManager instance;
    private SharedPreferences preferences;

    private SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return preferences.contains(KEY_TOKEN);
    }

    public void login(String token, String name) {
        preferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_NAME, name)
                .apply();
    }

    public void logout() {
        preferences.edit().clear().apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getName() {
        return preferences.getString(KEY_NAME, null);
    }
}
