package com.example.pulseaid.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private MaterialCardView cardSummary, cardManageUsers, cardBloodRequests, cardAnalytics, cardSettings;
    private MaterialCardView btnLogout;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } else {
            Log.e("AdminDashboard", "Main view is null, skipping insets");
        }

        cardSummary = findViewById(R.id.cardSummary);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardBloodRequests = findViewById(R.id.cardBloodRequests);
        cardAnalytics = findViewById(R.id.cardAnalytics);
        cardSettings = findViewById(R.id.cardSettings);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        prepareCardsForAnimation();
        animateCardsIn();

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        cardBloodRequests.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageRequestsActivity.class);
            startActivity(intent);
        });

        cardAnalytics.setOnClickListener(v -> {
            Toast.makeText(AdminDashboardActivity.this, "Analytics page coming soon!", Toast.LENGTH_SHORT).show();
        });

        cardSettings.setOnClickListener(v -> {
            Toast.makeText(AdminDashboardActivity.this, "Settings page coming soon!", Toast.LENGTH_SHORT).show();
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
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
        cardManageUsers.setTranslationY(offset);
        cardManageUsers.setAlpha(0f);
        cardBloodRequests.setTranslationY(offset);
        cardBloodRequests.setAlpha(0f);
        cardAnalytics.setTranslationY(offset);
        cardAnalytics.setAlpha(0f);
        cardSettings.setTranslationY(offset);
        cardSettings.setAlpha(0f);
    }

    private void animateCardsIn() {
        long delay = 100;
        long duration = 600;

        cardSummary.animate()
                .translationY(0f).alpha(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(duration)
                .setStartDelay(delay)
                .start();

        cardManageUsers.animate()
                .translationY(0f).alpha(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(duration)
                .setStartDelay(delay + 100)
                .start();

        cardBloodRequests.animate()
                .translationY(0f).alpha(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(duration)
                .setStartDelay(delay + 200)
                .start();

        cardAnalytics.animate()
                .translationY(0f).alpha(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(duration)
                .setStartDelay(delay + 300)
                .start();

        cardSettings.animate()
                .translationY(0f).alpha(1f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(duration)
                .setStartDelay(delay + 400)
                .start();
    }
}