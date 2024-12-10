package com.example.golosovanie;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_TOKEN = "user_token";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static SessionManager instance;

    private SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void login(String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_TOKEN, token);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public String getUserToken() {
        return sharedPreferences.getString(KEY_USER_TOKEN, null);
    }
}
