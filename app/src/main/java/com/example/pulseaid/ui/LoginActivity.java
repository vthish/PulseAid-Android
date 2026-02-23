package com.example.pulseaid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.pulseaid.R;
import com.example.pulseaid.viewmodel.LoginViewModel;
import com.example.pulseaid.data.User;
import com.example.pulseaid.ui.admin.AdminDashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordText, registerText;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        registerText = findViewById(R.id.registerText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to ForgotPasswordActivity
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        setupObservers();
    }

    private void setupObservers() {
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                loginButton.setText("LOGGING IN...");
                loginButton.setEnabled(false);
            } else {
                loginButton.setText("LOGIN");
                loginButton.setEnabled(true);
            }
        });

        loginViewModel.getLoginSuccessData().observe(this, user -> {
            if (user != null) {
                Toast.makeText(LoginActivity.this, "Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
                redirectUserBasedOnRole(user.getRole());
            }
        });

        loginViewModel.getLoginErrorData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email address is required!");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required!");
            passwordEditText.requestFocus();
            return;
        }

        loginViewModel.login(email, password);
    }

    private void redirectUserBasedOnRole(String role) {
        if (role == null) return;

        switch (role.toLowerCase()) {
            case "admin":
                Toast.makeText(this, "Redirecting to Admin Dashboard...", Toast.LENGTH_SHORT).show();
                // Navigate to Admin Dashboard
                Intent adminIntent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                startActivity(adminIntent);
                finish();
                break;
            case "blood_bank":
                Toast.makeText(this, "Redirecting to Blood Bank Dashboard...", Toast.LENGTH_SHORT).show();
                break;
            case "hospital":
                Toast.makeText(this, "Redirecting to Hospital Dashboard...", Toast.LENGTH_SHORT).show();
                break;
            case "donor":
                Toast.makeText(this, "Redirecting to Donor Dashboard...", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "Invalid Role!", Toast.LENGTH_SHORT).show();
        }
    }
}