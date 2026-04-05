package com.example.pulseaid.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pulseaid.R;
import com.example.pulseaid.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialCardView cardSummary, cardManageUsers, cardBloodRequests, cardAnalytics, cardSettings;
    private MaterialCardView btnLogout;
    private Button btnInfo;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        cardSummary = findViewById(R.id.cardSummary);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardBloodRequests = findViewById(R.id.cardBloodRequests);
        cardAnalytics = findViewById(R.id.cardAnalytics);
        cardSettings = findViewById(R.id.cardSettings);
        btnLogout = findViewById(R.id.btnLogout);
        btnInfo = findViewById(R.id.btnInfo);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnInfo.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAccountDetails.class);
            startActivity(intent);
        });

        cardManageUsers.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class)));
        cardBloodRequests.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, ManageRequestsActivity.class)));
        cardAnalytics.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AnalyticsActivity.class)));
        cardSettings.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminSettingsActivity.class)));

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_alerts) {
                startActivity(new Intent(AdminDashboardActivity.this, ManageRequestsActivity.class));
                return true;
            }
            if (itemId == R.id.nav_settings) {
                startActivity(new Intent(AdminDashboardActivity.this, AdminSettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}