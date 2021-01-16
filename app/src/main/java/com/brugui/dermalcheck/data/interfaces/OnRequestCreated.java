package com.brugui.dermalcheck.data.interfaces;

import com.brugui.dermalcheck.data.Result;

public interface OnRequestCreated {
    /**
     * Method called when a new Request is created
     * @param result
     */
    void OnRequestCreated(Result result);
}
