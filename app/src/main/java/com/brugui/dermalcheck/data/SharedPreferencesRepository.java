package com.brugui.dermalcheck.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.brugui.dermalcheck.data.model.LoggedInUser;

public class SharedPreferencesRepository {
    private static final String PREFERENCES_NAME = "DERMALCHECK";
    private static final String PREF_USER_UID = "UID";
    private static final String PREF_USER_EMAIL = "EMAIL";
    private static final String PREF_USER_NAME = "NAME";
    private static final String PREF_USER_PASSWORD = "PASSWORD";
    private static final String PREF_USER_ROL = "ROL";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * @param key
     * @return value or null
     */
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void putString(String key, String value) {
        sharedPreferences
                .edit()
                .putString(key, value)
                .apply();
    }


    public void saveUserLogged(LoggedInUser user) {
        sharedPreferences
                .edit()
                .putString(PREF_USER_UID, user.getUserId())
                .putString(PREF_USER_EMAIL, user.getEmail())
                .putString(PREF_USER_NAME, user.getDisplayName())
                .putString(PREF_USER_ROL, user.getRole())
                .apply();
    }

    public LoggedInUser getUserLogged() {
        LoggedInUser user = new LoggedInUser();
        user.setUserId(this.getString(PREF_USER_UID));
        if (user.getUserId() == null) {
            return null;
        }
        user.setEmail(this.getString(PREF_USER_EMAIL));
        if (user.getEmail() == null) {
            return null;
        }
        user.setDisplayName(this.getString(PREF_USER_NAME));
        user.setRole(this.getString(PREF_USER_ROL));
        return user;
    }
}
