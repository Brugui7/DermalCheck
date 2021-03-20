package com.brugui.dermalcheck.ui.request;

import androidx.annotation.Nullable;

import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;

/**
 * Request fetch result : success (request details) or error message.
 */
public class SingleRequestResult {
    @Nullable
    private Request success;
    @Nullable
    private Integer error;

    public SingleRequestResult(@Nullable Integer error) {
        this.error = error;
    }

    public SingleRequestResult(@Nullable Request success) {
        this.success = success;
    }

    @Nullable
    public Request getSuccess() {
        return success;
    }

    @Nullable
    public Integer getError() {
        return error;
    }
}