package com.pulseaid.viewmodel.donor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.pulseaid.data.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DonorDashboardViewModel extends ViewModel {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private MutableLiveData<String> donorName = new MutableLiveData<>();
    private MutableLiveData<Integer> donationCount = new MutableLiveData<>();
    private MutableLiveData<Integer> livesSavedCount = new MutableLiveData<>();
    private MutableLiveData<String> eligibilityStatus = new MutableLiveData<>();
    private MutableLiveData<Boolean> isProfileComplete = new MutableLiveData<>();

    public DonorDashboardViewModel() {
        fetchDonorData();
    }

    public LiveData<String> getDonorName() { return donorName; }
    public LiveData<Integer> getDonationCount() { return donationCount; }
    public LiveData<Integer> getLivesSaved() { return livesSavedCount; }
    public LiveData<String> getEligibilityStatus() { return eligibilityStatus; }
    public LiveData<Boolean> getProfileStatus() { return isProfileComplete; }

    private void fetchDonorData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(uid).addSnapshotListener((document, error) -> {
            if (error != null) return;

            if (document != null && document.exists()) {
                User user = document.toObject(User.class);
                if (user != null) {
                    donorName.setValue(user.getName());

                    isProfileComplete.setValue(user.getIsProfileComplete());

                    int count = 0;
                    donationCount.setValue(count);
                    livesSavedCount.setValue(count * 3);

                    String bloodGroup = document.getString("bloodGroup");
                    calculateDonorEligibility(bloodGroup);
                }
            }
        });
    }

    private void calculateDonorEligibility(String bloodGroup) {
        if (bloodGroup != null && !bloodGroup.isEmpty()) {
            eligibilityStatus.setValue("Ready to Save a Life");
        } else {
            eligibilityStatus.setValue("Complete your profile");
        }
    }
}