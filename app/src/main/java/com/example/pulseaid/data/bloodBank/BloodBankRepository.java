package com.example.pulseaid.data.bloodBank;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class BloodBankRepository {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public BloodBankRepository() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // Update Profile method
    public void updateBloodBankProfile(Map<String, Object> profileData, OnCompleteListener<Void> onCompleteListener) {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(userId)
                    .update(profileData)
                    .addOnCompleteListener(onCompleteListener);
        }
    }
}