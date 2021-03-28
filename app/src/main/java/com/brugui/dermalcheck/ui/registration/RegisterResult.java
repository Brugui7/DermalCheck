package com.brugui.dermalcheck.ui.registration;

import androidx.annotation.Nullable;

import com.brugui.dermalcheck.data.model.LoggedInUser;

/**
 * Registration result : success (user details) or error message.
 */
class RegisterResult {
    @Nullable
    private Integer error;

    RegisterResult(@Nullable Integer error) {
        this.error = error;
    }

    @Nullable
    Integer getError() {
        return error;
    }
}