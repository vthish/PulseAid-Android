package com.example.pulseaid.ui.hospital;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pulseaid.R;

import java.util.ArrayList;

public class AvailabilityActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability);

        String type = getIntent().getStringExtra("TYPE");
        int needed = Integer.parseInt(getIntent().getStringExtra("UNITS"));

        ArrayList<String> results = new ArrayList<>();

        // Simulation Data (Meeka real system eke Firestore walin ganna ona) [cite: 44, 157]
        // Example: Bank A gawa 4i, Bank B gawa 8i thiyenawa.
        int bankA_Stock = 4;
        int bankB_Stock = 8;

        int remaining = needed;

        // FIFO Logic ekata anuwa mulinma ena bank eken gannawa [cite: 27, 51, 139]
        if (remaining > 0 && bankA_Stock > 0) {
            int take = Math.min(bankA_Stock, remaining);
            results.add("Bank Colombo: " + take + " Units allocated");
            remaining -= take;
        }

        if (remaining > 0 && bankB_Stock > 0) {
            int take = Math.min(bankB_Stock, remaining);
            results.add("Bank Kandy: " + take + " Units allocated");
            remaining -= take;
        }

        if (remaining > 0) {
            results.add("Shortage: " + remaining + " Units not found!");
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, results);
        ((ListView)findViewById(R.id.listBanks)).setAdapter(adapter);
    }
}