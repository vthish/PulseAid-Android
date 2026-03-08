package com.example.pulseaid.ui.hospital;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    public static List<DeliveryHistoryModel> globalHistoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        findViewById(R.id.btnBackFromHistory).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        DeliveryHistoryAdapter adapter = new DeliveryHistoryAdapter(globalHistoryList);
        rv.setAdapter(adapter);
    }
}