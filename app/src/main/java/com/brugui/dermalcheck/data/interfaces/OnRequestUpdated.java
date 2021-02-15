package com.brugui.dermalcheck.data.interfaces;

import com.brugui.dermalcheck.data.Result;

public interface OnRequestUpdated {
    /**
     * Method called when a request is updated
     * @param result
     */
    void onRequestUpdated(Result result);
}
