package com.example.pulseaid.ui.hospital;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;

import java.util.ArrayList;
import java.util.List;

public class ConfirmDeliveriesActivity extends AppCompatActivity {

    private static List<PendingDeliveryModel> pendingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_deliveries);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        RecyclerView rvConfirmDeliveries = findViewById(R.id.rvConfirmDeliveries);

        if (pendingList.isEmpty()) {
            pendingList.add(new PendingDeliveryModel("O+", "04", "Colombo Blood Bank", "Arrived 5 mins ago"));
            pendingList.add(new PendingDeliveryModel("A-", "02", "Kandy Blood Bank", "Arrived 15 mins ago"));
            pendingList.add(new PendingDeliveryModel("B+", "08", "Galle Blood Bank", "Arrived 1 hour ago"));
        }

        ConfirmDeliveryAdapter adapter = new ConfirmDeliveryAdapter(pendingList);
        rvConfirmDeliveries.setAdapter(adapter);
    }
}