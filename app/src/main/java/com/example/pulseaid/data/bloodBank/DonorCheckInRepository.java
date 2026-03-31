package com.example.pulseaid.data.bloodBank;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DonorCheckInRepository {

    private final FirebaseFirestore db;

    public DonorCheckInRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface EligibilityCallback {
        void onResult(boolean isEligible, String message);
        void onFailure(String error);
    }

    public void verifyDonorEligibility(String donorId, EligibilityCallback callback) {
        // Checking the "Users" collection for the specific donor ID
        db.collection("Users").document(donorId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();

                        if (doc.exists()) {
                            // Assuming you save the last donation date as a Timestamp in Firestore
                            Date lastDonationDate = doc.getDate("lastDonationDate");

                            if (lastDonationDate == null) {
                                // If there is no record, they are eligible
                                callback.onResult(true, "Eligible: No previous donation record found.");
                            } else {
                                // Calculate the difference in days
                                long diffInMillies = Math.abs(new Date().getTime() - lastDonationDate.getTime());
                                long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                                if (diffInDays >= 90) {
                                    callback.onResult(true, "Eligible: Last donation was " + diffInDays + " days ago.");
                                } else {
                                    long daysRemaining = 90 - diffInDays;
                                    callback.onResult(false, "Not Eligible: Must wait " + daysRemaining + " more days.");
                                }
                            }
                        } else {
                            callback.onFailure("Donor ID not found in the database.");
                        }
                    } else {
                        callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Error connecting to database.");
                    }
                });
    }
}