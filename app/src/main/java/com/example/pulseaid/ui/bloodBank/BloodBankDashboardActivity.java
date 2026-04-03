package com.example.pulseaid.ui.bloodBank;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pulseaid.R;
import com.example.pulseaid.ui.LoginActivity;
import com.example.pulseaid.viewmodel.bloodBank.BloodBankDashboardViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BloodBankDashboardActivity extends AppCompatActivity {
    private RecyclerView recyclerViewActivities;
    private ActivityAdapter activityAdapter;
    private List<ActivityItem> activityList;
    private TextView txtTotalStock, txtPending, txtTodayApoinment, txtExpireAlert;
    private BloodBankDashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_bank_dashboard);
        txtTotalStock = findViewById(R.id.txtTotalStock);
        txtPending = findViewById(R.id.txtPending);
        txtTodayApoinment = findViewById(R.id.txtTodayApoinment);
        txtExpireAlert = findViewById(R.id.txtExpireAlert);
        recyclerViewActivities = findViewById(R.id.recyclerViewActivities);
        recyclerViewActivities.setLayoutManager(new LinearLayoutManager(this));
        activityList = new ArrayList<>();
        activityAdapter = new ActivityAdapter(activityList);
        recyclerViewActivities.setAdapter(activityAdapter);
        CardView cardInventory = findViewById(R.id.cardInventory);
        CardView cardRequests = findViewById(R.id.cardRequests);
        CardView cardAppointments = findViewById(R.id.cardAppointments);
        CardView cardAlerts = findViewById(R.id.cardAlerts);
        MaterialCardView btnLogout = findViewById(R.id.btnLogout);
        MaterialCardView btnProfile = findViewById(R.id.btnProfile);
        viewModel = new ViewModelProvider(this).get(BloodBankDashboardViewModel.class);
        observeDashboardData();
        cardInventory.setOnClickListener(v -> {
            addRecentActivity("Viewed Stock Status", "Checked current blood inventory levels.", android.R.drawable.ic_menu_sort_by_size);
            startActivity(new Intent(this, StockMonitosActivity.class));
        });
        cardRequests.setOnClickListener(v -> {
            addRecentActivity("Viewed Pending Orders", "Checked hospital requests that need approval.", android.R.drawable.ic_menu_recent_history);
            startActivity(new Intent(this, HospitleRequestHandlingActivity.class));
        });
        cardAppointments.setOnClickListener(v -> {
            addRecentActivity("Viewed Appointments", "Checked today's donor appointments.", android.R.drawable.ic_menu_today);
            startActivity(new Intent(this, DonerCheckInQueryActivity.class));
        });
        cardAlerts.setOnClickListener(v -> {
            addRecentActivity("Viewed Alerts", "Checked expiring blood stock alerts.", android.R.drawable.ic_dialog_alert);
            startActivity(new Intent(this, ExpiringAlertsActivity.class));
        });
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> startActivity(new Intent(this, BloodBankProfileActivity.class)));
        }
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadDashboardData();
    }

    private void observeDashboardData() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                txtTotalStock.setText("..."); txtPending.setText("..."); txtTodayApoinment.setText("..."); txtExpireAlert.setText("...");
            }
        });
        viewModel.getTotalStock().observe(this, stock -> {
            if (stock != null) txtTotalStock.setText(stock + " Units");
        });
        viewModel.getPendingOrders().observe(this, pending -> {
            if (pending != null) txtPending.setText(String.format(Locale.getDefault(), "%02d", pending));
        });
        viewModel.getTodayAppointments().observe(this, appt -> {
            if (appt != null) txtTodayApoinment.setText(String.format(Locale.getDefault(), "%02d", appt));
        });
        viewModel.getExpireAlerts().observe(this, alerts -> {
            if (alerts != null) txtExpireAlert.setText(String.format(Locale.getDefault(), "%02d", alerts));
        });
    }

    private void addRecentActivity(String title, String desc, int iconRes) {
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        activityList.add(0, new ActivityItem(title, desc, currentTime, iconRes));
        if (activityList.size() > 5) activityList.remove(activityList.size() - 1);
        activityAdapter.notifyDataSetChanged();
    }

    private static class ActivityItem {
        String title, description, time; int iconResource;
        public ActivityItem(String title, String description, String time, int iconResource) {
            this.title = title; this.description = description; this.time = time; this.iconResource = iconResource;
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
        private List<ActivityItem> items;
        public ActivityAdapter(List<ActivityItem> items) { this.items = items; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_activity, parent, false);
            return new ViewHolder(view);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActivityItem item = items.get(position);
            holder.txtTitle.setText(item.title); holder.txtDesc.setText(item.description); holder.txtTime.setText(item.time); holder.imgIcon.setImageResource(item.iconResource);
            if(item.title.contains("Alerts")) holder.imgIcon.setColorFilter(android.graphics.Color.parseColor("#D32F2F"));
            else if(item.title.contains("Orders")) holder.imgIcon.setColorFilter(android.graphics.Color.parseColor("#F57C00"));
            else holder.imgIcon.setColorFilter(android.graphics.Color.parseColor("#1976D2"));
        }
        @Override public int getItemCount() { return items.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtTitle, txtDesc, txtTime; ImageView imgIcon;
            ViewHolder(View itemView) {
                super(itemView);
                txtTitle = itemView.findViewById(R.id.txtActivityTitle); txtDesc = itemView.findViewById(R.id.txtActivityDesc);
                txtTime = itemView.findViewById(R.id.txtActivityTime); imgIcon = itemView.findViewById(R.id.imgActivityIcon);
            }
        }
    }
}