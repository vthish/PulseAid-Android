package com.example.pulseaid.ui.hospital;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pulseaid.R;

public class RequestFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_form);

        Button btnFindBanks = findViewById(R.id.btnCheck);

        btnFindBanks.setOnClickListener(v -> {
            Intent intent = new Intent(RequestFormActivity.this, AvailabilityActivity.class);
            startActivity(intent);
        });
    }
}