package com.example.pulseaid.ui.bloodBank;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.pulseaid.R;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class StockMonitosActivity extends AppCompatActivity {

    private CardView cardAPos, cardANeg, cardBPos, cardBNeg, cardABPos, cardABNeg, cardOPos, cardONeg;
    private MaterialCardView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_monitos);

        initializeViews();
        setupClickListeners();
        setupBack();
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

        btnBack = findViewById(R.id.btnBack); // ✅ from updated XML
    }

    private void setupBack() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
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

        // ✅ Mock data (replace with Firebase later)
        inventoryList.add(new BloodPacket("PKT-105", "2026-06-12"));
        inventoryList.add(new BloodPacket("PKT-202", "2026-03-25")); // earliest
        inventoryList.add(new BloodPacket("PKT-309", "2026-04-05"));

        // ✅ FIFO: sort by expiry date (real date compare)
        Collections.sort(inventoryList, new Comparator<BloodPacket>() {
            @Override
            public int compare(BloodPacket p1, BloodPacket p2) {
                Date d1 = parseDateSafe(p1.getExpiryDate());
                Date d2 = parseDateSafe(p2.getExpiryDate());

                // if parse fail, keep original order safely
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;

                return d1.compareTo(d2);
            }
        });

        StringBuilder message = new StringBuilder();
        message.append("Priority: Expiring Soon (FIFO)\n\n");

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

    // Supports common formats: "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy"
    private Date parseDateSafe(String s) {
        if (s == null) return null;

        String[] patterns = new String[]{
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "MM/dd/yyyy"
        };

        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.US);
                sdf.setLenient(false);
                return sdf.parse(s);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private static class BloodPacket {
        private final String packetId;
        private final String expiryDate;

        public BloodPacket(String packetId, String expiryDate) {
            this.packetId = packetId;
            this.expiryDate = expiryDate;
        }

        public String getPacketId() { return packetId; }
        public String getExpiryDate() { return expiryDate; }
    }
}