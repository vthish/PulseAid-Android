package com.example.pulseaid.ui.bloodBank;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulseaid.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class HospitleRequestHandlingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private MaterialCardView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospitle_request_handling);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerHospitalRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<HospitalRequest> requestList = new ArrayList<>();
        requestList.add(new HospitalRequest("General Hospital", "A+", "5 Units"));
        requestList.add(new HospitalRequest("Base Hospital", "O-", "2 Units"));

        adapter = new RequestAdapter(requestList);
        recyclerView.setAdapter(adapter);
    }

    public static class HospitalRequest {
        String name, type, qty;

        HospitalRequest(String name, String type, String qty) {
            this.name = name;
            this.type = type;
            this.qty = qty;
        }
    }
}