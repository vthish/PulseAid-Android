package com.example.pulseaid.ui.hospital;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import java.util.ArrayList;
import java.util.List;

public class ActiveRequestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_requests);

        findViewById(R.id.btnBackFromActive).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvActiveRequests);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<ActiveRequestModel> list = new ArrayList<>();

        list.add(new ActiveRequestModel("AB+", "02", "07 Mar 2026", "Awaiting Bank Confirmation...", 0));

        list.add(new ActiveRequestModel("B-", "03", "06 Mar 2026", "Accepted by Colombo Bank. Arriving tomorrow.", 1));

        list.add(new ActiveRequestModel("O+", "04", "07 Mar 2026", "Dispatched! Check 'Delivery Alert' on dashboard.", 2));

        ActiveRequestAdapter adapter = new ActiveRequestAdapter(list);
        rv.setAdapter(adapter);
    }
}