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

public class DelivaryStatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivary_status);

        ImageView btnBack = findViewById(R.id.btnBackFromStatus);
        btnBack.setOnClickListener(v -> finish());

        // ලිස්ට් එක සකස් කිරීම
        RecyclerView rvExpectedDeliveries = findViewById(R.id.rvExpectedDeliveries);

        // අද දවසට එන්න තියෙන ඩිලිවරි (Dummy Data)
        List<PendingDeliveryModel> todayExpected = new ArrayList<>();
        todayExpected.add(new PendingDeliveryModel("O+", "04", "Colombo Blood Bank", "10:30 AM"));
        todayExpected.add(new PendingDeliveryModel("AB-", "01", "Kandy Blood Bank", "01:15 PM"));
        todayExpected.add(new PendingDeliveryModel("B+", "05", "Galle Blood Bank", "04:45 PM"));

        ExpectedDeliveryAdapter adapter = new ExpectedDeliveryAdapter(todayExpected);
        rvExpectedDeliveries.setAdapter(adapter);


    }
}