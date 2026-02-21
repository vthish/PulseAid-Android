package com.example.pulseaid.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
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

    private MaterialCardView cardSummary, cardManageDonors, cardManageBloodBanks, cardManageHospitals, cardBloodRequests;
    private MaterialCardView btnLogout;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        // Handle Window Insets
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize Views
        cardSummary = findViewById(R.id.cardSummary);
        cardManageDonors = findViewById(R.id.cardManageDonors);
        cardManageBloodBanks = findViewById(R.id.cardManageBloodBanks);
        cardManageHospitals = findViewById(R.id.cardManageHospitals);
        cardBloodRequests = findViewById(R.id.cardBloodRequests);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        prepareCardsForAnimation();
        animateCardsIn();

        // Logout Click Listener
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Dashboard Card Click Listeners for Navigation
        cardManageDonors.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageDonorsActivity.class);
            startActivity(intent);
        });

        cardManageBloodBanks.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageBloodBanksActivity.class);
            startActivity(intent);
        });

//        cardManageHospitals.setOnClickListener(v -> {
//            Intent intent = new Intent(AdminDashboardActivity.this, ManageHospitalsActivity.class);
//            startActivity(intent);
//        });
//
//        cardBloodRequests.setOnClickListener(v -> {
//            Intent intent = new Intent(AdminDashboardActivity.this, BloodRequestsActivity.class);
//            startActivity(intent);
//        });

        // Setup Bottom Navigation Bar Clicks
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on Dashboard
                return true;
            } else if (itemId == R.id.nav_alerts) {
                Toast.makeText(this, "System Alerts clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Admin Settings clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void prepareCardsForAnimation() {
        float offset = 100f;

        cardSummary.setTranslationY(offset);
        cardSummary.setAlpha(0f);
        cardManageDonors.setTranslationY(offset);
        cardManageDonors.setAlpha(0f);
        cardManageBloodBanks.setTranslationY(offset);
        cardManageBloodBanks.setAlpha(0f);
        cardManageHospitals.setTranslationY(offset);
        cardManageHospitals.setAlpha(0f);
        cardBloodRequests.setTranslationY(offset);
        cardBloodRequests.setAlpha(0f);
    }

    private void animateCardsIn() {
        long delay = 100;
        long duration = 600;

        cardSummary.animate().translationY(0f).alpha(1f).setInterpolator(new OvershootInterpolator()).setDuration(duration).setStartDelay(delay).start();
        cardManageDonors.animate().translationY(0f).alpha(1f).setInterpolator(new OvershootInterpolator()).setDuration(duration).setStartDelay(delay + 100).start();
        cardManageBloodBanks.animate().translationY(0f).alpha(1f).setInterpolator(new OvershootInterpolator()).setDuration(duration).setStartDelay(delay + 150).start();
        cardManageHospitals.animate().translationY(0f).alpha(1f).setInterpolator(new OvershootInterpolator()).setDuration(duration).setStartDelay(delay + 200).start();
        cardBloodRequests.animate().translationY(0f).alpha(1f).setInterpolator(new OvershootInterpolator()).setDuration(duration).setStartDelay(delay + 250).start();
    }
}