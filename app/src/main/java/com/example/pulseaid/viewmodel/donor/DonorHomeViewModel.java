package com.example.pulseaid.viewmodel.donor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pulseaid.data.donor.DonorRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DonorHomeViewModel extends ViewModel {

    private final DonorRepository repository;
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<Integer> livesSaved = new MutableLiveData<>();
    private final MutableLiveData<String> eligibilityStatus = new MutableLiveData<>();
    private final MutableLiveData<Integer> eligibilityProgress = new MutableLiveData<>();

    public DonorHomeViewModel() {
        this.repository = new DonorRepository();
    }

    public void loadDashboardData(String userId) {
        repository.getProfile(userId, new DonorRepository.OnProfileCallback() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (doc.exists()) {
                    userName.setValue(doc.getString("name"));

                    Long count = doc.getLong("donationCount");
                    livesSaved.setValue(count != null ? count.intValue() : 0);

                    calculateEligibility(doc.getString("lastDonationDate"));
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    private void calculateEligibility(String lastDateStr) {
        if (lastDateStr == null || lastDateStr.isEmpty()) {
            eligibilityStatus.setValue("You are Eligible!");
            eligibilityProgress.setValue(100);
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date lastDate = sdf.parse(lastDateStr);
            long diffInMs = Math.abs(new Date().getTime() - lastDate.getTime());
            long daysPassed = TimeUnit.DAYS.convert(diffInMs, TimeUnit.MILLISECONDS);

            int remainingDays = (int) (90 - daysPassed);

            if (remainingDays <= 0) {
                eligibilityStatus.setValue("You are Eligible!");
                eligibilityProgress.setValue(100);
            } else {
                eligibilityStatus.setValue("Eligible in " + remainingDays + " Days");
                int progress = (int) ((daysPassed / 90.0) * 100);
                eligibilityProgress.setValue(progress);
            }
        } catch (ParseException e) {
            eligibilityStatus.setValue("Status Unknown");
            eligibilityProgress.setValue(0);
        }
    }

    public LiveData<String> getUserName() { return userName; }
    public LiveData<Integer> getLivesSaved() { return livesSaved; }
    public LiveData<String> getEligibilityStatus() { return eligibilityStatus; }
    public LiveData<Integer> getEligibilityProgress() { return eligibilityProgress; }
}