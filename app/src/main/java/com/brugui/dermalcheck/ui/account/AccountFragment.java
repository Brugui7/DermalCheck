package com.brugui.dermalcheck.ui.account;

import android.content.Context;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.SharedPreferencesRepository;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    private EditText etDisplayName;
    private ConstraintLayout clContainer;
    private AccountViewModel viewModel;
    private static final String TAG = "Logger AccFragment";


    public AccountFragment() {

    }

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new AccountViewModel();
        viewModel.fetchUserdata(viewModel.getUserLogged().getUid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        etDisplayName = view.findViewById(R.id.etDisplayName);
        clContainer = view.findViewById(R.id.clContainer);
        Button btnUpdateData = view.findViewById(R.id.btnUpdateData);


        viewModel.getUserData().observe(getViewLifecycleOwner(), loggedInUser -> {
            etDisplayName.setText(loggedInUser.getDisplayName());
        });
        btnUpdateData.setOnClickListener(listenerBtnUpdateData);
        return view;
    }

    private final View.OnClickListener listenerBtnUpdateData = view -> {
        LoggedInUser user = viewModel.getUserData().getValue();
        if (user == null) {
            return;
        }
        Log.d(TAG, user.toString());
        user.setDisplayName(etDisplayName.getText().toString().trim());
        viewModel.updateUserData(user, result -> {
            if (result instanceof Result.Error) {
                Objects.requireNonNull(CustomSnackbar.make(
                        clContainer,
                        getString(R.string.error_creating_request),
                        Snackbar.LENGTH_SHORT,
                        null,
                        R.drawable.ic_error_outline,
                        null,
                        getContext().getColor(R.color.accent)
                )).show();
                return;
            }

            Objects.requireNonNull(CustomSnackbar.make(
                    clContainer,
                    getString(R.string.request_updated_successfully),
                    Snackbar.LENGTH_SHORT,
                    null,
                    R.drawable.ic_check_circle_outline,
                    null,
                    getContext().getColor(R.color.success)
            )).show();
        });
    };


}