package com.example.pulseaid.ui.hospital;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pulseaid.R;
import java.util.ArrayList;

public class AvailabilityActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability);

        String type = getIntent().getStringExtra("TYPE");
        if (type == null) {
            type = "Unknown";
        }

        String unitsString = getIntent().getStringExtra("UNITS");
        int needed = 0;

        if (unitsString != null && !unitsString.isEmpty()) {
            needed = Integer.parseInt(unitsString);
        } else {
            needed = 1;
        }

        ArrayList<String> results = new ArrayList<>();
        int bankA_Stock = 4;
        int bankB_Stock = 8;
        int remaining = needed;

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