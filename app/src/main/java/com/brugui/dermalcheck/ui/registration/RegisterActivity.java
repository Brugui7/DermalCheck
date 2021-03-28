package com.brugui.dermalcheck.ui.registration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.ui.MainActivity;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.brugui.dermalcheck.ui.login.LoginActivity;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;


public class RegisterActivity extends AppCompatActivity {


    private EditText etName, etNick, username, password, etConfirmPassword;
    private ProgressBar loadingProgressBar;
    private RegisterViewModel registerViewModel;
    private ConstraintLayout container;
    private Button btnRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etName = findViewById(R.id.etName);
        etNick = findViewById(R.id.etNick);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        container = findViewById(R.id.container);
        loadingProgressBar = findViewById(R.id.loading);
        btnRegister = findViewById(R.id.btnRegister);

        registerViewModel = new RegisterViewModel();
        registerViewModel.getRegisterFormState().observe(this, this::onFormChanged);
        registerViewModel.getRegisterResult().observe(this, this::onRegisterResult);

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                registerViewModel.registerDataChanged(
                        username.getText().toString(),
                        password.getText().toString(),
                        etConfirmPassword.getText().toString()
                );
            }
        };

        username.addTextChangedListener(afterTextChangedListener);
        password.addTextChangedListener(afterTextChangedListener);
        etConfirmPassword.addTextChangedListener(afterTextChangedListener);

        btnRegister.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            registerViewModel.register(
                    etName.getText().toString(),
                    etNick.getText().toString(),
                    username.getText().toString(),
                    password.getText().toString()
            );
        });

    }


    private void onFormChanged(RegisterFormState registerFormState) {
        if (registerFormState == null) {
            return;
        }

        btnRegister.setEnabled(registerFormState.isDataValid());
        username.setError(null);
        password.setError(null);
        if (registerFormState.getEmailError() != null) {
            username.setError(getString(registerFormState.getEmailError()));
        }

        if (registerFormState.getPasswordError() != null) {
            password.setError(getString(registerFormState.getPasswordError()));
        }
    }

    private void onRegisterResult(RegisterResult registerResult) {
        if (registerResult == null) {
            return;
        }

        loadingProgressBar.setVisibility(View.GONE);
        if (registerResult.getError() != null) {
            CustomSnackbar.make(container, getString(registerResult.getError()),
                    Snackbar.LENGTH_SHORT,
                    null,
                    R.drawable.ic_error_outline,
                    null,
                    getColor(R.color.accent)
            ).show();
            return;
        }

        btnRegister.setEnabled(false);
        CustomSnackbar.make(container, getString(R.string.registration_success),
                Snackbar.LENGTH_SHORT,
                null,
                R.drawable.ic_check_circle_outline,
                null,
                getColor(R.color.success)
        ).addCallback(new BaseTransientBottomBar.BaseCallback<CustomSnackbar>() {
            @Override
            public void onDismissed(CustomSnackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).show();

    }
}