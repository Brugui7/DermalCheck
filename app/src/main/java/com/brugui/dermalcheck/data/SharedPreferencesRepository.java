package com.brugui.dermalcheck.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.brugui.dermalcheck.data.model.LoggedInUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesRepository {
    private static final String PREFERENCES_NAME = "DERMALCHECK";
    private static final String PREF_USER_UID = "UID";
    private static final String PREF_USER_EMAIL = "EMAIL";
    private static final String PREF_USER_NAME = "NAME";
    private static final String PREF_USER_PASSWORD = "PASSWORD";
    private static final String PREF_USER_ROL = "ROL";
    private static final String PREF_DIAGNOSED_REQUESTS = "DIAGNOSED_REQUESTS";
    private static final String PREF_MATCHING_DIAGNOSED_REQUESTS = "MATCHING_DIAGNOSED_REQUESTS";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * @param key String
     * @return value or null
     */
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    /**
     *
     * @param key String
     * @return int value | 0
     */
    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public Set<String> getStringSet(String key) {
        return sharedPreferences.getStringSet(key, null);
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
                .putString(PREF_USER_UID, user.getUid())
                .putString(PREF_USER_EMAIL, user.getEmail())
                .putString(PREF_USER_NAME, user.getDisplayName())
                .putString(PREF_USER_ROL, user.getRole())
                .putString(PREF_USER_PASSWORD, user.getPassword())
                .putInt(PREF_DIAGNOSED_REQUESTS, user.getRequestsDiagnosed())
                .putInt(PREF_MATCHING_DIAGNOSED_REQUESTS, user.getMatchingRequestsDiagnosed())
                .apply();
    }


    public void clearUserPassword() {
        sharedPreferences
                .edit()
                .putString(PREF_USER_PASSWORD, null)
                .apply();
    }

    public LoggedInUser getUserLogged() {
        LoggedInUser user = new LoggedInUser();
        user.setUid(this.getString(PREF_USER_UID));
        if (user.getUid() == null) {
            return null;
        }
        user.setEmail(this.getString(PREF_USER_EMAIL));
        if (user.getEmail() == null) {
            return null;
        }
        user.setDisplayName(this.getString(PREF_USER_NAME));
        user.setRole(this.getString(PREF_USER_ROL));
        user.setPassword(this.getString(PREF_USER_PASSWORD));
        user.setMatchingRequestsDiagnosed(this.getInt(PREF_MATCHING_DIAGNOSED_REQUESTS));
        user.setRequestsDiagnosed(this.getInt(PREF_DIAGNOSED_REQUESTS));
        return user;
    }
}
