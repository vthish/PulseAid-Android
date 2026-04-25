package com.pulseaid.ui.donor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.pulseaid.R;
import com.pulseaid.viewmodel.donor.RegisterViewModel;

public class DonorRegisterActivity extends AppCompatActivity {

    private EditText nameBox, emailBox, passBox, confirmPassBox;
    private Button regBtn;
    private TextView goLoginText;
    private RegisterViewModel regViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_register);


        regViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        nameBox = findViewById(R.id.nameEditText);
        emailBox = findViewById(R.id.emailEditText);
        passBox = findViewById(R.id.passwordEditText);
        confirmPassBox = findViewById(R.id.confirmPasswordEditText);
        regBtn = findViewById(R.id.registerButton);
        goLoginText = findViewById(R.id.backToLoginText);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterProcess();
            }
        });

        goLoginText.setOnClickListener(v -> finish());
        setupStatusObservers();
    }

    private void startRegisterProcess() {
        String name = nameBox.getText().toString().trim();
        String email = emailBox.getText().toString().trim();
        String pass = passBox.getText().toString().trim();
        String confPass = confirmPassBox.getText().toString().trim();


        if (TextUtils.isEmpty(name)) { nameBox.setError("Name Required"); return; }
        if (TextUtils.isEmpty(email)) { emailBox.setError("Email Required"); return; }
        if (pass.length() < 6) { passBox.setError("Less Characters"); return; }
        if (!pass.equals(confPass)) { confirmPassBox.setError("Password not  match"); return; }


        regViewModel.registerDonor(name, email, pass);
    }

    private void setupStatusObservers() {

        regViewModel.getLoadingStatus().observe(this, isLoading -> {
            if (isLoading) {
                regBtn.setText("REGISTERING");
                regBtn.setEnabled(false);
            } else {
                regBtn.setText("REGISTER");
                regBtn.setEnabled(true);
            }
        });


        regViewModel.getSuccessStatus().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Registration Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        regViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
}