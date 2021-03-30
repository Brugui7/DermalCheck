package com.brugui.dermalcheck.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.SharedPreferencesRepository;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Rol;
import com.brugui.dermalcheck.ui.MainActivity;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.brugui.dermalcheck.ui.login.LoginActivity;
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
    //private FrameLayout flStatisticsPlaceholder;
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
        Button btnCloseSession = view.findViewById(R.id.btnCloseSession);


        viewModel.getUserData().observe(getViewLifecycleOwner(), loggedInUser -> {
            etDisplayName.setText(loggedInUser.getDisplayName());
            if (loggedInUser.getRole().equalsIgnoreCase(Rol.GENERAL_ROL)) {
                FragmentTransaction ft = getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction();
                ft.replace(R.id.flStatisticsPlaceholder, GeneralMedicStatisticsFragment.newInstance(loggedInUser));
                ft.commit();
            }
        });
        btnUpdateData.setOnClickListener(listenerBtnUpdateData);
        btnCloseSession.setOnClickListener(view1 -> {
            viewModel.closeSession();
            SharedPreferencesRepository sharedPreferencesRepository = new SharedPreferencesRepository(getActivity());
            sharedPreferencesRepository.clearUserPassword();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
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