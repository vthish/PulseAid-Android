package com.example.pulseaid.ui.bloodBank;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.pulseaid.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StockMonitosActivity extends AppCompatActivity {

    private CardView cardAPos, cardANeg, cardBPos, cardBNeg, cardABPos, cardABNeg, cardOPos, cardONeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_monitos);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        cardAPos = findViewById(R.id.cardAPositive);
        cardANeg = findViewById(R.id.cardANegative);
        cardBPos = findViewById(R.id.cardBPositive);
        cardBNeg = findViewById(R.id.cardBNegative);
        cardABPos = findViewById(R.id.cardABPositive);
        cardABNeg = findViewById(R.id.cardABNegative);
        cardOPos = findViewById(R.id.cardOPositive);
        cardONeg = findViewById(R.id.cardONegative);
    }

    private void setupClickListeners() {
        cardAPos.setOnClickListener(v -> showFIFOInventoryDialog("A+"));
        cardANeg.setOnClickListener(v -> showFIFOInventoryDialog("A-"));
        cardBPos.setOnClickListener(v -> showFIFOInventoryDialog("B+"));
        cardBNeg.setOnClickListener(v -> showFIFOInventoryDialog("B-"));
        cardABPos.setOnClickListener(v -> showFIFOInventoryDialog("AB+"));
        cardABNeg.setOnClickListener(v -> showFIFOInventoryDialog("AB-"));
        cardOPos.setOnClickListener(v -> showFIFOInventoryDialog("O+"));
        cardONeg.setOnClickListener(v -> showFIFOInventoryDialog("O-"));
    }

    private void showFIFOInventoryDialog(String bloodGroup) {
        ArrayList<BloodPacket> inventoryList = new ArrayList<>();
        // Mock Data - Expiry dates different formats can be used
        inventoryList.add(new BloodPacket("PKT-105", "2026-06-12"));
        inventoryList.add(new BloodPacket("PKT-202", "2026-03-25")); // This should come first
        inventoryList.add(new BloodPacket("PKT-309", "2026-04-05"));

        // FIFO Logic:
        Collections.sort(inventoryList, new Comparator<BloodPacket>() {
            @Override
            public int compare(BloodPacket p1, BloodPacket p2) {
                return p1.getExpiryDate().compareTo(p2.getExpiryDate());
            }
        });

        StringBuilder message = new StringBuilder();
        message.append("Priority: Expiring Soon (FIFO):\n\n");
        for (BloodPacket packet : inventoryList) {
            message.append("ID: ").append(packet.getPacketId())
                    .append("\nExpiry: ").append(packet.getExpiryDate())
                    .append("\n--------------------------\n");
        }

        new AlertDialog.Builder(this)
                .setTitle(bloodGroup + " Stock Status")
                .setMessage(message.toString())
                .setPositiveButton("Dismiss", null)
                .show();
    }

    private static class BloodPacket {
        private String packetId;
        private String expiryDate;

        public BloodPacket(String packetId, String expiryDate) {
            this.packetId = packetId;
            this.expiryDate = expiryDate;
        }

        public String getPacketId() { return packetId; }
        public String getExpiryDate() { return expiryDate; }
    }
}