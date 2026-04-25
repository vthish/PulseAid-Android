package com.pulseaid.viewmodel.bloodBank;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pulseaid.data.bloodBank.BloodBankRepository;

import java.util.Map;

public class BloodBankProfileViewModel extends ViewModel {
    private BloodBankRepository repository;

    // LiveData for UI observation
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private MutableLiveData<String> updateError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public BloodBankProfileViewModel() {
        repository = new BloodBankRepository();
    }

    public LiveData<Boolean> getUpdateSuccess() { return updateSuccess; }
    public LiveData<String> getUpdateError() { return updateError; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void updateProfile(Map<String, Object> profileData) {
        isLoading.setValue(true);
        repository.updateBloodBankProfile(profileData, task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                updateSuccess.setValue(true);
            } else {
                updateError.setValue(task.getException() != null ? task.getException().getMessage() : "Profile update failed.");
            }
        });
    }
}